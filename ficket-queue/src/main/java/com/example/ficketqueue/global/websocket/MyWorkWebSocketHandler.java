package com.example.ficketqueue.global.websocket;

import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class MyWorkWebSocketHandler extends TextWebSocketHandler {

    private final ClientNotificationService clientNotificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    private final QueueService queueService;

    @Override
    public void afterConnectionEstablished(@NotNull WebSocketSession session) throws Exception {
        String userId = session.getHandshakeHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 존재하지 않습니다. 세션 ID: {}", session.getId());
            session.close();
            return;
        }

        // 기존 세션 정보 확인 및 복구
        if (clientNotificationService.isUserSessionExists(userId)) {
            log.info("사용자 {}의 기존 세션이 발견되었습니다. 상태 복구 진행 중...", userId);
            clientNotificationService.removeAndCreateNewSession(userId, session);
        } else {
            clientNotificationService.registerSession(userId, session);
            log.info("사용자 {}의 새 WebSocket 연결이 설정되었습니다.", userId);
        }
    }


    @Override
    public void afterConnectionClosed(@NotNull WebSocketSession session, @NotNull CloseStatus status) {
        String userId = parsingUserId(session);
        // 세션을 즉시 제거
        clientNotificationService.removeSession(userId);

        // 지연 삭제 처리
        scheduler.schedule(() -> {
            if (!clientNotificationService.isUserSessionExists(userId)) {
                queueService.releaseSlotByUserId(userId);
                log.info("5초 동안 재연결이 없어 사용자 {}의 WebSocket 세션이 최종적으로 제거되었습니다.", userId);
            }
        }, 5, TimeUnit.SECONDS); // 10초 대기
    }

    private String parsingUserId(WebSocketSession session) {
        String uri = Objects.requireNonNull(session.getUri()).toString();
        String[] parts = uri.split("/");
        return parts[parts.length - 1];
    }

}