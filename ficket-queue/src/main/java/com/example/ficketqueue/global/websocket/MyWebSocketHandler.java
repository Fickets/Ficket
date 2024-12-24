package com.example.ficketqueue.global.websocket;

import com.example.ficketqueue.queue.enums.QueueStatus;
import com.example.ficketqueue.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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

    private final QueueService queueService;
    private final ConcurrentHashMap<String, CopyOnWriteArraySet<WebSocketSession>> eventSessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String eventId = getEventIdFromUri(session);
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");
        log.info("Handshake Headers: {}", session.getHandshakeHeaders());
        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 누락됨. 세션 ID: {}", session.getId());
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }


        sessionUserMap.put(session.getId(), userId);
        eventSessions.computeIfAbsent(eventId, key -> new CopyOnWriteArraySet<>()).add(session);

        log.info("WebSocket 연결: 세션 ID={}, 사용자 ID={}, 이벤트 ID={}", session.getId(), userId, eventId);
        sendInitialMessage(session, userId, eventId);
    }

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("메시지 수신: {}", payload);

        if ("queue-status".equalsIgnoreCase(payload)) {
            String eventId = getEventIdFromUri(session);
            sendQueueStatus(session, eventId).subscribe();
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String eventId = getEventIdFromUri(session);
        String userId = sessionUserMap.remove(session.getId());
        eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).remove(session);

        if (userId != null) {
            queueService.leaveQueue(userId, eventId).subscribe();
            log.info("사용자 제거: Redis에서 사용자 ID={}, 이벤트 ID={}", userId, eventId);
        }

        log.info("WebSocket 연결 종료: 세션 ID={}, 이벤트 ID={}", session.getId(), eventId);
    }

    @Scheduled(fixedRate = 5000)
    public void sendPeriodicQueueUpdates() {
        eventSessions.forEach((eventId, sessions) ->
                sessions.forEach(session -> {
                    if (session.isOpen()) {
                        sendQueueStatus(session, eventId).subscribe();
                    }
                })
        );
    }

    private Mono<Void> sendQueueStatus(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            log.warn("사용자를 찾을 수 없음: 세션 ID={}", session.getId());
            return Mono.empty();
        }

        return queueService.getQueueSize(eventId)
                .flatMap(totalQueue -> queueService.getUserPosition(userId, eventId)
                        .flatMap(position -> {
                            QueueStatus status = getQueueStatus(position);
                            boolean canEnter = (status == QueueStatus.ALMOST_DONE && position == 1 && queueService.canEnterQueue(eventId));

                            if (canEnter && queueService.occupySlot(userId, eventId)) {
                                log.info("작업 슬롯 할당: 사용자 ID={}, 이벤트 ID={}", userId, eventId);
                                return sendWebSocketMessage(session, buildResponse(userId, eventId, position, totalQueue, QueueStatus.COMPLETED))
                                        .then(Mono.fromRunnable(() -> closeSession(session)));
                            }

                            return sendWebSocketMessage(session, buildResponse(userId, eventId, position, totalQueue, status));
                        })
                );
    }

    private void sendInitialMessage(WebSocketSession session, String userId, String eventId) {
        String message = buildResponse(userId, eventId, -1, 999999999, QueueStatus.IN_PROGRESS);
        sendWebSocketMessage(session, message).subscribe();
    }

    private void closeSession(WebSocketSession session) {
        try {
            session.close(CloseStatus.NORMAL);
            log.info("WebSocket 연결 종료: 세션 ID={}", session.getId());
        } catch (IOException e) {
            log.error("WebSocket 연결 종료 실패: {}", e.getMessage());
        }
    }

    private QueueStatus getQueueStatus(long userPosition) {
        if (userPosition == -1) {
            return QueueStatus.CANCELLED;
        } else if (userPosition <= 100) {
            return QueueStatus.ALMOST_DONE;
        }
        return QueueStatus.WAITING;
    }

    private Mono<Void> sendWebSocketMessage(WebSocketSession session, String message) {
        return Mono.fromRunnable(() -> {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (IOException e) {
                log.error("WebSocket 메시지 전송 실패: {}", e.getMessage());
            }
        });
    }

    private String buildResponse(String userId, String eventId, long position, long totalQueue, QueueStatus status) {
        return String.format(
                "{\"userId\": \"%s\", \"eventId\": \"%s\", \"myWaitingNumber\": %d, \"totalWaitingNumber\": %d, \"queueStatus\": \"%s\"}",
                userId, eventId, position, totalQueue, status.getDescription()
        );
    }

    private String getEventIdFromUri(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }
}
