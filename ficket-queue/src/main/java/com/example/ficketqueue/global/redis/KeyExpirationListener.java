package com.example.ficketqueue.global.redis;

import com.example.ficketqueue.queue.enums.WorkStatus;
import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.SlotService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class KeyExpirationListener implements MessageListener {

    private final SlotService slotService;
    private final ClientNotificationService clientNotificationService;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = message.toString();

        if (expiredKey.startsWith("ficket:workspace:")) {
            String[] parts = expiredKey.split(":");
            String eventId = parts[2];
            String userId = parts[3];

            log.info("작업 공간 TTL 만료: 이벤트 ID={}, 사용자 ID={}", eventId, userId);

            clientNotificationService.notifyUser(userId, WorkStatus.ORDER_RIGHT_LOST)
                    .then(slotService.releaseSlot(eventId))
                    .doOnSuccess(unused -> log.info("작업 공간 처리 완료: 이벤트 ID={}, 사용자 ID={}", eventId, userId))
                    .doOnError(error -> log.error("작업 공간 처리 실패: 이벤트 ID={}, 사용자 ID={}, 에러={}", eventId, userId, error.getMessage()))
                    .subscribe();
        } else if (expiredKey.startsWith("ficket:user:")) {
            String[] parts = expiredKey.split(":");
            String userId = parts[2];

            log.info("좌석 선점 TTL 만료: 사용자 ID={}", userId);

            // 클라이언트에 좌석 선점 해제 메시지 발송
            clientNotificationService.notifyUser(userId, WorkStatus.SEAT_RESERVATION_RELEASED)
                    .doOnSuccess(unused -> log.info("좌석 선점 해제 메시지 전송 완료: 사용자 ID={}", userId))
                    .doOnError(error -> log.error("좌석 선점 해제 실패: 사용자 ID={}, 에러={}", userId, error.getMessage()))
                    .subscribe();
        }
    }
}
