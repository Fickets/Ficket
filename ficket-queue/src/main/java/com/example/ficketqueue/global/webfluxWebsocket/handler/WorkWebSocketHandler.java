package com.example.ficketqueue.global.webfluxWebsocket.handler;

import com.example.ficketqueue.global.utils.WebSocketUrlParser;
import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.socket.WebSocketHandler;
import org.springframework.web.reactive.socket.WebSocketMessage;
import org.springframework.web.reactive.socket.WebSocketSession;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Component
@RequiredArgsConstructor
public class WorkWebSocketHandler implements WebSocketHandler {

    private final SlotService slotService;
    private final ClientNotificationService clientNotificationService;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(8);

    @NotNull
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        String userId = session.getHandshakeInfo().getHeaders().getFirst("X-User-Id");

        if (userId == null || userId.isEmpty()) {
            log.warn("X-User-Id 헤더가 존재하지 않습니다. 세션 ID: {}", session.getId());
            return session.close();
        }

        // AtomicBoolean: 특정 메시지 수신 여부 확인
        AtomicBoolean preventRelease = new AtomicBoolean(false);

        // 기존 세션 정보 확인 및 복구
        if (clientNotificationService.isUserSessionExists(userId)) {
            log.info("사용자 {}의 기존 세션이 발견되었습니다. 상태 복구 진행 중...", userId);
            clientNotificationService.removeAndCreateNewSession(userId, session);
            preventRelease.set(false);
        } else {
            clientNotificationService.registerSession(userId, session);
            log.info("사용자 {}의 새 WebSocket 연결이 설정되었습니다.", userId);
        }


        return session.receive()
                .map(WebSocketMessage::getPayloadAsText)
                .doOnNext(message -> {
                    if ("BEFORE_STEP".equals(message) || "NEXT_STEP".equals(message)) {
                        log.info("사용자 {}가 페이지 이동 중이므로 좌석 해제를 취소합니다. 메시지: {}", userId, message);
                        preventRelease.set(true); // ✅ 특정 메시지를 수신하면 해제 방지
                    }
                })
                .doFinally(signalType -> handleConnectionClosed(session, userId, preventRelease))
                .then();
    }

    private void handleConnectionClosed(WebSocketSession session, String userId, AtomicBoolean preventRelease) {
        // WebSocket URL에서 eventId, eventScheduleId 추출
        String[] pathInfo = WebSocketUrlParser.getEventAndScheduleFromUri(session);
        if (pathInfo == null || pathInfo.length < 2) {
            log.error("WebSocket URL에서 eventId 또는 eventScheduleId를 추출할 수 없습니다.");
            return;
        }
        String eventId = pathInfo[0];
        String eventScheduleId = pathInfo[1];

        // 세션을 즉시 제거
        clientNotificationService.removeSession(userId);

        // 3초 지연 후 슬롯 및 좌석 해제 처리
        scheduler.schedule(() -> {
            if (!clientNotificationService.isUserSessionExists(userId) && !preventRelease.get()) {
                // 특정 메시지를 받지 않았고, 3초 후에도 재접속이 없으면 해제 실행
                slotService.releaseSlotByEventIdAndUserId(eventId, userId)
                        .doOnSuccess(unused -> {
                            log.info("사용자 {}의 슬롯 해제 후 좌석이 비동기적으로 해제됩니다. eventScheduleId: {}", userId, eventScheduleId);

                            // 슬롯 해제 후 좌석 해제 실행
                            slotService.unLockSeats(eventScheduleId, userId)
                                    .doOnSuccess(unused2 -> log.info("좌석 해제 완료: 사용자 {}, eventScheduleId: {}", userId, eventScheduleId))
                                    .doOnError(error -> log.error("좌석 해제 중 오류 발생: 사용자 {}, eventScheduleId: {}, 오류 {}", userId, eventScheduleId, error.getMessage()))
                                    .subscribe(); // 비동기 실행
                        })
                        .doOnError(e -> log.error("슬롯 해제 중 오류 발생: 사용자 {}, eventScheduleId: {}, 오류 {}", userId, eventScheduleId, e.getMessage()))
                        .subscribe();
            } else {
                log.info("사용자 {}가 3초 내에 다시 접속했거나 특정 메시지를 받아 해제를 취소합니다.", userId);
            }
        }, 3, TimeUnit.SECONDS);
    }
}
