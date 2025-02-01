package com.example.ficketqueue.global.redis;

import com.example.ficketqueue.queue.enums.WorkStatus;
import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;


@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener {

    private final ClientNotificationService clientNotificationService;
    private final SlotService slotService;

    public Mono<Void> handleExpireKey(String expiredKey) {
        log.info("TTL 만료된 키 감지: {}", expiredKey);

        if (expiredKey.startsWith("ficket:workspace:")) {
            return handleWorkspaceExpiration(expiredKey);
        } else if (expiredKey.startsWith("ficket:user:")) {
            return handleUserExpiration(expiredKey);
        }

        return Mono.empty();

    }

    private Mono<Void> handleWorkspaceExpiration(String expiredKey) {
        String[] parts = expiredKey.split(":");
        String eventId = parts[2];
        String userId = parts[3];

        log.info("작업 공간 TTL 만료: 이벤트 ID={}, 사용자 ID={}", eventId, userId);

        return clientNotificationService.notifyUser(userId, WorkStatus.ORDER_RIGHT_LOST)
                .then(slotService.releaseSlotByEventIdAndUserId(eventId, userId));
    }

    private Mono<Void> handleUserExpiration(String expiredKey) {
        String[] parts = expiredKey.split(":");
        String userId = parts[2];

        log.info("좌석 선점 TTL 만료: 사용자 ID={}", userId);

        return clientNotificationService.notifyUser(userId, WorkStatus.SEAT_RESERVATION_RELEASED)
                .doOnSuccess(unused -> log.info("좌석 선점 해제 메시지 전송 완료: 사용자 ID={}", userId))
                .doOnError(error -> log.error("좌석 선점 해제 실패: 사용자 ID={}, 에러={}", userId, error.getMessage()));
    }
}