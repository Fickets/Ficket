package com.example.ficketqueue.global.redis;

import com.example.ficketqueue.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.data.redis.core.RedisKeyExpiredEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisKeyExpirationListener {

    private final QueueRepository queueRepository;

    /**
     * workingUserKey TTL 만료 시 처리
     * - currentNumber 감소
     */
    @EventListener
    public void onKeyExpired(RedisKeyExpiredEvent event) {
        String expiredKey = event.getSource().toString();

        // queue:{eventId}:working:{userId} 형식만 처리
        if (!expiredKey.contains(":working:")) {
            return;
        }

        try {
            // queue:{eventId}:working:{userId}
            String[] parts = expiredKey.split(":");
            if (parts.length != 4) {
                log.warn("[REDIS TTL] invalid key format: {}", expiredKey);
                return;
            }

            String eventId = parts[1];
            String userId = parts[3];

            queueRepository.decrementCurrentNumber(eventId);

            log.info(
                    "[REDIS TTL] workingUser expired → currentNumber-- | eventId={}, userId={}",
                    eventId, userId
            );


        } catch (Exception e) {
            log.error("[REDIS TTL] failed to handle expired key={}", expiredKey, e);
        }
    }
}
