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
    private final ConcurrentHashMap<String, String> sessionUserMap = new ConcurrentHashMap<>(); // 세션 ID -> 사용자 ID 매핑

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

    @Override
    protected void handleTextMessage(@NotNull WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        log.info("메시지 수신: {}", payload);
        String eventId = getEventIdFromUri(session);

        if (payload.equalsIgnoreCase("queue-status")) {
            sendQueueStatus(session, eventId).subscribe();
        }
    }

    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String eventId = getEventIdFromUri(session);
        String userId = sessionUserMap.remove(session.getId());
        eventSessions.getOrDefault(eventId, new CopyOnWriteArraySet<>()).remove(session);

        if (userId != null) {
            // Redis에서 사용자 제거
            queueService.leaveQueue(userId, eventId).subscribe();
            log.info("사용자 제거: Redis에서 사용자 ID={}, 이벤트 ID={}", userId, eventId);
        }

        log.info("WebSocket 연결 종료: 세션 ID={}, 이벤트 ID={}", session.getId(), eventId);
    }

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

    private Mono<Void> sendQueueStatus(WebSocketSession session, String eventId) {
        String userId = sessionUserMap.get(session.getId());
        if (userId == null) {
            log.warn("세션 ID에 대한 사용자 ID를 찾을 수 없습니다: 세션 ID={}", session.getId());
            return Mono.empty();
        }

        return queueService.getQueueSize(eventId)
                .flatMap(totalQueue -> queueService.getUserPosition(userId, eventId)
                        .flatMap(position -> {
                            QueueStatus status = getQueueStatus(position);

                            // 작업 가능한 슬롯이 비었고 사용자가 순번 1번일 때
                            if (status == QueueStatus.ALMOST_DONE && position == 1 && queueService.canEnterQueue(eventId)) {
                                if (queueService.occupySlot(userId, eventId)) { // 슬롯 점유 성공
                                    log.info("사용자가 작업 슬롯으로 입장: 사용자 ID={}, 이벤트 ID={}", userId, eventId);

                                    // 사용자에게 작업 가능 메시지 전송
                                    String completeMessage = String.format(
                                            "{\"userId\": \"%s\", \"eventId\": \"%s\", \"myWaitingNumber\": %d, \"totalWaitingNumber\": %d, \"queueStatus\": \"%s\"}",
                                            userId,
                                            eventId,
                                            position,
                                            totalQueue,
                                            QueueStatus.COMPLETED.getDescription()
                                    );

                                    sendWebSocketMessage(session, completeMessage).subscribe();

                                    // WebSocket 연결 종료
                                    try {
                                        session.close(CloseStatus.NORMAL);
                                        log.info("WebSocket 연결 종료: 사용자 ID={}, 이벤트 ID={}", userId, eventId);
                                    } catch (IOException e) {
                                        log.error("WebSocket 연결 종료 실패: {}", e.getMessage());
                                    }
                                }
                                return Mono.empty();
                            }

                            // 대기 상태 메시지 전송
                            String jsonResponse = String.format(
                                    "{\"userId\": \"%s\", \"eventId\": \"%s\", \"myWaitingNumber\": %d, \"totalWaitingNumber\": %d, \"queueStatus\": \"%s\"}",
                                    userId,
                                    eventId,
                                    position,
                                    totalQueue,
                                    status.getDescription()
                            );
                            return sendWebSocketMessage(session, jsonResponse);
                        })
                ).then();
    }

    private QueueStatus getQueueStatus(long userPosition) {
        if (userPosition == -1) {
            return QueueStatus.CANCELLED;
        } else if (userPosition <= 1000) {
            return QueueStatus.ALMOST_DONE;
        } else {
            return QueueStatus.WAITING;
        }
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

    private String getEventIdFromUri(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }
}