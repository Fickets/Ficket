package com.example.ficketqueue.global.webfluxWebsocket.handler;

import com.example.ficketqueue.global.utils.WebSocketUrlParser;
import com.example.ficketqueue.queue.enums.QueueStatus;
import com.example.ficketqueue.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> eventSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String eventId = WebSocketUrlParser.getInfoFromUri(session);
        String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 누락됨. 세션 ID: {}", session.getId());
            return session.close().then();
        }

        sessionUserMap.put(session.getId(), userId);
        eventSessions.computeIfAbsent(eventId, key -> new CopyOnWriteArraySet<>()).add(session);

        log.info("WebSocket 연결: 세션 ID={}, 사용자 ID={}, 이벤트 ID={}", session.getId(), userId, eventId);

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

    private void handleMessage(WebSocketSession session, String eventId, String payload) {
        log.info("메시지 수신: {}", payload);
        if ("queue-status".equalsIgnoreCase(payload)) {
            sendQueueStatus(session, eventId).subscribe();
        }
    }

    private void handleDisconnect(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.remove(session.getId());
        eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).remove(session);

        if (userId != null) {
            queueService.leaveQueue(userId, eventId).subscribe();
            log.info("사용자 제거: Redis에서 사용자 ID={}, 이벤트 ID={}", userId, eventId);
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

        return queueService.getQueueSize(eventId)
                .flatMap(totalQueue -> queueService.getUserPosition(userId, eventId)
                        .flatMap(position -> queueService.canEnterQueue(eventId)
                                .flatMap(canEnter -> {
                                    QueueStatus status = getQueueStatus(position);
                                    if (canEnter && status == QueueStatus.ALMOST_DONE && position == 1) {
                                        return attemptSlotOccupation(session, userId, eventId, position, totalQueue, status);
                                    }
                                    return sendWebSocketMessage(session, buildResponse(userId, eventId, position, totalQueue, status));
                                })
                        )
                );
    }

    private Mono<Void> attemptSlotOccupation(WebSocketSession session, String userId, String eventId, long position, long totalQueue, QueueStatus status) {
        return queueService.occupySlot(userId, eventId)
                .flatMap(occupied -> {
                    if (occupied) {
                        log.info("작업 슬롯 할당 성공: 사용자 ID={}, 이벤트 ID={}", userId, eventId);
                        return sendWebSocketMessage(session, buildResponse(userId, eventId, position, totalQueue, QueueStatus.COMPLETED))
                                .doOnTerminate(() -> closeSession(session));
                    } else {
                        log.warn("작업 슬롯 할당 실패: 사용자 ID={}, 이벤트 ID={}", userId, eventId);
                        // 점유 실패 시 아무 작업도 수행하지 않음
                        return Mono.empty();
                    }
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
