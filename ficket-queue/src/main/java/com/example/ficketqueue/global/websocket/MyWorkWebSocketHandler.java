package com.example.ficketqueue.global.websocket;

import com.example.ficketqueue.queue.service.ClientNotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyWorkWebSocketHandler extends TextWebSocketHandler {

    private final ClientNotificationService clientNotificationService;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 존재하지 않습니다. 세션 ID: {}", session.getId());
            session.close();
            return;
        }

        // WebSocket 세션 등록
        clientNotificationService.registerSession(userId, session);
        log.info("사용자 {}의 WebSocket 연결이 설정되었습니다. 세션 ID: {}", userId, session.getId());
    }


    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String userId = parsingUserId(session);
        clientNotificationService.removeSession(userId);
        log.info("사용자 {}의 WebSocket 연결이 종료되었습니다.", userId);
    }

    private String parsingUserId(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }

}