package com.example.ficketqueue.global.webfluxWebsocket.handler;

import com.example.ficketqueue.global.utils.WebSocketUrlParser;
import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkWebSocketHandler implements WebSocketHandler {

    private final ClientNotificationService clientNotificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);
    private final QueueService queueService;

    @NotNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 존재하지 않습니다. 세션 ID: {}", session.getId());
            return session.close();
        }

        // 기존 세션 정보 확인 및 복구
        if (clientNotificationService.isUserSessionExists(userId)) {
            log.info("사용자 {}의 기존 세션이 발견되었습니다. 상태 복구 진행 중...", userId);
            clientNotificationService.removeAndCreateNewSession(userId, session);
        } else {
            clientNotificationService.registerSession(userId, session);
            log.info("사용자 {}의 새 WebSocket 연결이 설정되었습니다.", userId);
        }

        return session.receive()
                .doFinally(signalType -> handleConnectionClosed(session))
                .then();
    }

    private void handleConnectionClosed(WebSocketSession session) {
        String userId = WebSocketUrlParser.getInfoFromUri(session);
        // 세션을 즉시 제거
        clientNotificationService.removeSession(userId);

        // 5초 지연 후 슬롯 해제 처리
        scheduler.schedule(() -> {
            if (!clientNotificationService.isUserSessionExists(userId)) {
                queueService.releaseSlotByUserId(userId)
                        .doOnSuccess(unused -> log.info("사용자 {}의 슬롯이 해제되었습니다.", userId))
                        .doOnError(e -> log.error("슬롯 해제 중 오류 발생: 사용자 {}, 오류 {}", userId, e.getMessage()))
                        .subscribe(); // 비동기 실행
            }
        }, 5, TimeUnit.SECONDS); // 5초 지연
    }
}
