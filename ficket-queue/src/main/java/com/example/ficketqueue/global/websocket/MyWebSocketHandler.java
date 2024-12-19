package com.example.ficketqueue.global.websocket;

import com.example.ficketqueue.enums.QueueStatus;
import com.example.ficketqueue.global.utils.KeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * WebSocket 핸들러 클래스.
 * 대기열 상태를 관리하고 실시간으로 사용자에게 정보를 전달하는 역할을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class MyWebSocketHandler extends TextWebSocketHandler {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> eventSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>(); // 세션 ID -> 사용자 ID 매핑

    /**
     * WebSocket 연결이 설정되었을 때 호출됩니다.
     * 헤더에서 사용자 ID를 추출하고 이벤트 세션에 등록합니다.
     *
     * @param session WebSocket 세션
     */
    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String eventId = getEventIdFromUri(session);

        // 헤더에서 X-User-Id 추출
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");
        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 존재하지 않습니다. 세션 ID: {}", session.getId());
            session.close();
            return;
        }

        sessionUserMap.put(session.getId(), userId);
        eventSessions.putIfAbsent(eventId, new CopyOnWriteArraySet<>());
        eventSessions.get(eventId).add(session);

        log.info("WebSocket 연결됨: 세션 ID={}, 사용자 ID={}, 이벤트 ID={}", session.getId(), userId, eventId);
        session.sendMessage(new TextMessage("{\"message\": \"연결이 성공적으로 설정되었습니다.\"}"));
    }

    /**
     * 클라이언트로부터 메시지를 수신했을 때 호출됩니다.
     * 현재는 'queue-status' 메시지에 대해 대기열 상태를 전송합니다.
     *
     * @param session WebSocket 세션
     * @param message 수신된 메시지
     */
    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("메시지 수신: {}", payload);
        String eventId = getEventIdFromUri(session);

        if (payload.equalsIgnoreCase("queue-status")) {
            sendQueueStatus(session, eventId).subscribe();
        }
    }

    /**
     * WebSocket 연결이 종료되었을 때 호출됩니다.
     * 세션 및 사용자 매핑을 제거합니다.
     *
     * @param session WebSocket 세션
     * @param status 연결 종료 상태
     */
    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String eventId = getEventIdFromUri(session);
        sessionUserMap.remove(session.getId()); // 세션 ID 제거
        eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).remove(session);

        log.info("WebSocket 연결 종료: 세션 ID={}, 이벤트 ID={}", session.getId(), eventId);
    }

    /**
     * 주기적으로 대기열 상태를 모든 세션에 전송합니다.
     * 5초마다 실행됩니다.
     */
    @Scheduled(fixedRate = 5000)
    public void sendPeriodicQueueUpdates() {
        for (String eventId : eventSessions.keySet()) {
            for (WebSocketSession session : eventSessions.get(eventId)) {
                if (session.isOpen()) {
                    sendQueueStatus(session, eventId).subscribe();
                }
            }
        }
    }

    /**
     * 대기열 상태를 전송합니다.
     * Redis에서 사용자 대기 위치를 조회하고 상태를 클라이언트에 전송합니다.
     *
     * @param session WebSocket 세션
     * @param eventId 이벤트 ID
     * @return Mono<Void> 비동기 작업
     */
    private Mono<Void> sendQueueStatus(WebSocketSession session, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        ReactiveListOperations<String, String> listOps = reactiveRedisTemplate.opsForList();

        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            log.warn("세션 ID에 대한 사용자 ID를 찾을 수 없습니다: 세션 ID={}", session.getId());
            return Mono.empty();
        }

        return listOps.size(redisKey)
                .flatMap(totalQueue -> listOps.indexOf(redisKey, userId) // Redis 리스트에서 사용자 ID의 위치 조회
                        .defaultIfEmpty(-1L) // 찾지 못하면 -1L 반환
                        .flatMap(position -> {
                            long userPosition = position >= 0 ? position + 1 : -1;

                            QueueStatus status = getQueueStatus(userPosition);

                            String jsonResponse = String.format(
                                    "{\"userId\": \"%s\", \"eventId\": \"%s\", \"myWaitingNumber\": %d, \"totalWaitingNumber\": %d, \"queueStatus\": \"%s\"}",
                                    userId,
                                    eventId,
                                    userPosition,
                                    totalQueue,
                                    status.getDescription()
                            );

                            return sendWebSocketMessage(session, jsonResponse);
                        })
                ).then();
    }

    /**
     * 대기 상태에 따른 QueueStatus를 반환합니다.
     *
     * @param userPosition 사용자 위치
     * @return QueueStatus 대기 상태
     */
    private QueueStatus getQueueStatus(long userPosition) {
        if (userPosition == -1) {
            return QueueStatus.CANCELLED;
        } else if (userPosition <= 1000) { // 대기 순서가 1000 이하일 때 거의 완료 상태
            return QueueStatus.ALMOST_DONE;
        } else {
            return QueueStatus.WAITING;
        }
    }

    /**
     * WebSocket 메시지를 클라이언트에 전송합니다.
     *
     * @param session WebSocket 세션
     * @param message 전송할 메시지
     * @return Mono<Void> 비동기 작업
     */
    private Mono<Void> sendWebSocketMessage(WebSocketSession session, String message) {
        return Mono.fromRunnable(() -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
            }
        });
    }

    /**
     * WebSocket URI에서 이벤트 ID를 추출합니다.
     *
     * @param session WebSocket 세션
     * @return 이벤트 ID
     */
    private String getEventIdFromUri(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        return parts[parts.length - 1]; // URL 마지막 부분이 eventId라고 가정
    }
}
