package com.example.ficketqueue.global.webfluxWebsocket.handler;

import com.example.ficketqueue.global.utils.WebSocketUrlParser;
import com.example.ficketqueue.queue.enums.QueueStatus;
import com.example.ficketqueue.queue.service.QueueService;
import com.example.ficketqueue.queue.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebFlux 기반 WebSocket 핸들러 클래스.
 * 대기열 상태를 관리하고 실시간으로 사용자에게 정보를 전달하는 역할을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class QueueStatusWebSocketHandler implements WebSocketHandler {

    private final QueueService queueService;
    private final SlotService slotService;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> eventSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Boolean> monitoringStatus = new ConcurrentHashMap<>();

    @NotNull
    @Override
    public Mono<Void> handle(@NotNull WebSocketSession session) {
        String eventId = WebSocketUrlParser.getInfoFromUri(session);
        String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 누락됨. 세션 ID: {}", session.getId());
            return session.close().then();
        }

        sessionUserMap.put(session.getId(), userId);
        eventSessions.computeIfAbsent(eventId, key -> new CopyOnWriteArraySet<>()).add(session);

        log.info("WebSocket 연결: 세션 ID={}, 사용자 ID={}, 이벤트 ID={}", session.getId(), userId, eventId);

        startMonitorIfNeeded(eventId);

        // 상태 업데이트 주기적으로 실행
        Mono<Void> periodicUpdates = sendQueueStatusPeriodically(session, eventId);

        // 메시지 수신 처리
        Mono<Void> messageHandling = session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(payload -> handleMessage(session, eventId, payload))
                .then();

        // WebSocket 종료 처리
        Mono<Void> disconnectionHandling = Mono.fromRunnable(() -> handleDisconnect(session, eventId));

        return Mono.when(periodicUpdates, messageHandling)
                .publishOn(Schedulers.boundedElastic())
                .doFinally(signal -> disconnectionHandling.subscribe());
    }

    private void startMonitorIfNeeded(String eventId) {
        if (monitoringStatus.putIfAbsent(eventId, true) != null) {
            log.info("이벤트 ID={}에 대한 모니터링이 이미 실행 중입니다.", eventId);
            return;
        }

        log.info("이벤트 ID={}에 대한 모니터링 시작", eventId);

        monitorAndHandleQueue(eventId)
                .doFinally(signal -> {
                    log.info("이벤트 ID={} 모니터링 종료", eventId);
                    monitoringStatus.remove(eventId);
                })
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe();
    }

    private Mono<Void> sendQueueStatusPeriodically(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            log.warn("사용자를 찾을 수 없음: 세션 ID={}", session.getId());
            closeSession(session);
            return Mono.empty();
        }

        return Flux.interval(Duration.ofSeconds(5)) // 5초 간격으로 상태 전송
                .takeWhile(tick -> session.isOpen()) // 세션이 열려 있는 동안만 실행
                .flatMap(tick -> sendQueueStatus(session, eventId)) // 상태 업데이트
                .then();
    }

    private Mono<Void> monitorAndHandleQueue(String eventId) {
        return Flux.interval(Duration.ofSeconds(5)) // 5초 주기로 슬롯 상태 확인
                .takeWhile(tick -> eventSessions.containsKey(eventId) && !eventSessions.get(eventId).isEmpty())
                .flatMap(tick -> queueService.canEnterSlot(eventId)
                        .flatMapMany(canEnter -> {
                            if (canEnter) {
                                return processQueue(eventId);
                            }
                            return Flux.empty();
                        })
                )
                .then()
                .doOnTerminate(() -> log.info("이벤트 ID={} 모니터링 작업이 종료되었습니다.", eventId));
    }

    private Flux<Void> processQueue(String eventId) {
        return slotService.getAvailableSlots(eventId) // 현재 남은 슬롯 수 확인
                .flatMapMany(availableSlots -> {
                    if (availableSlots > 0) {
                        return Flux.range(1, Math.toIntExact(availableSlots)) // 남은 슬롯 수만큼 반복
                                .flatMap(slot -> queueService.getNextUserInQueue(eventId) // 대기열에서 사용자 가져오기
                                        .flatMap(userId -> {
                                            if (userId != null) {
                                                return slotService.occupySlot(userId, eventId)
                                                        .publishOn(Schedulers.boundedElastic())
                                                        .doOnNext(occupied -> {
                                                            if (occupied) {
                                                                // 상태 메시지 생성
                                                                String message = buildResponse(userId, eventId, -1, -1, QueueStatus.COMPLETED);

                                                                // 사용자 세션 찾기
                                                                WebSocketSession userSession = eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>())
                                                                        .stream()
                                                                        .filter(session -> sessionUserMap.get(session.getId()).equals(userId))
                                                                        .findFirst()
                                                                        .orElse(null);

                                                                // 메시지 전송
                                                                if (userSession != null) {
                                                                    sendWebSocketMessage(userSession, message).subscribe();
                                                                } else {
                                                                    log.warn("사용자 {}의 WebSocket 세션을 찾을 수 없음: 이벤트 ID={}", userId, eventId);
                                                                }


                                                                log.info("사용자 {} 슬롯 점유 & workspace 진입 성공: 이벤트 ID={}", userId, eventId);
                                                            } else {
                                                                log.warn("사용자 {} 슬롯 점유 실패: 이벤트 ID={}", userId, eventId);
                                                            }
                                                        })
                                                        .then();
                                            }
                                            log.warn("대기열에서 사용자 ID를 가져올 수 없음: 이벤트 ID={}", eventId);
                                            return Mono.empty();
                                        })
                                );
                    }
                    log.info("입장 가능한 슬롯이 없음: 이벤트 ID={}", eventId);
                    return Flux.empty();
                });
    }

    private void handleMessage(WebSocketSession session, String eventId, String payload) {
        log.info("메시지 수신: {}", payload);
    }

    private void handleDisconnect(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.remove(session.getId());
        eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).remove(session);

        if (userId != null) {
            queueService.leaveQueue(userId, eventId).subscribe();
            log.info("사용자 제거: Redis에서 사용자 ID={}, 이벤트 ID={}", userId, eventId);
        }

        if (eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).isEmpty()) {
            log.info("모든 세션이 종료되어 이벤트 모니터링을 중지합니다: 이벤트 ID={}", eventId);
            monitoringStatus.remove(eventId);
        }

        log.info("WebSocket 연결 종료: 세션 ID={}, 이벤트 ID={}", session.getId(), eventId);
    }

    private Mono<Void> sendQueueStatus(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            log.warn("사용자를 찾을 수 없음: 세션 ID={}", session.getId());
            closeSession(session);
            return Mono.empty();
        }

        // Redis에서 대기열 크기 및 사용자 위치 가져오기
        return queueService.getQueueSize(eventId)
                .flatMap(totalQueue -> queueService.getUserPosition(userId, eventId)
                        .flatMap(position -> {
                            QueueStatus status = getQueueStatus(position); // 사용자 상태 계산
                            String message = buildResponse(userId, eventId, position, totalQueue, status); // 메시지 생성
                            return sendWebSocketMessage(session, message); // WebSocket 메시지 전송
                        })
                )
                .doOnError(error -> log.error("대기열 상태 전송 실패: 세션 ID={}, 이벤트 ID={}, 오류={}", session.getId(), eventId, error.getMessage()))
                .onErrorResume(error -> {
                    closeSession(session); // 오류 발생 시 세션 닫기
                    return Mono.empty();
                });
    }

    private Mono<Void> sendWebSocketMessage(WebSocketSession session, String message) {
        return session.send(Mono.just(session.textMessage(message)));
    }

    private void closeSession(WebSocketSession session) {
        session.close().doOnError(e -> log.error("WebSocket 연결 종료 실패: {}", e.getMessage())).subscribe();
    }

    private QueueStatus getQueueStatus(long userPosition) {
        if (userPosition == -1) {
            return QueueStatus.CANCELLED;
        } else if (userPosition <= 100) {
            return QueueStatus.ALMOST_DONE;
        }
        return QueueStatus.WAITING;
    }

    private String buildResponse(String userId, String eventId, long position, long totalQueue, QueueStatus status) {
        return String.format(
                "{\"userId\": \"%s\", \"eventId\": \"%s\", \"myWaitingNumber\": %d, \"totalWaitingNumber\": %d, \"queueStatus\": \"%s\"}",
                userId, eventId, position, totalQueue, status.getDescription()
        );
    }
}
