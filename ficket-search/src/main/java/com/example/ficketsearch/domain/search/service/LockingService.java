package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockingService {

    private final RedissonClient redissonClient;

    private static final String FULL_INDEX_LOCK = "INDEXING_LOCK";

    /**
     * 전체 색인 락 획득 시도 (timeout 내에 시도 → leaseTime 동안 유지)
     */
    public boolean tryLock(long waitTimeSec, long leaseTimeSec) {
        RLock lock = redissonClient.getLock(FULL_INDEX_LOCK);

        try {
            boolean locked = lock.tryLock(waitTimeSec, leaseTimeSec, TimeUnit.SECONDS);
            if (locked) {
                log.info("[LOCK ACQUIRED] 전체 색인 락 획득 성공");
            } else {
                log.info("[LOCK FAILED] 이미 다른 서버에서 전체 색인 중");
            }
            return locked;

        } catch (Exception e) {
            log.error("전체 색인 락 획득 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 락 해제
     */
    public void unlock() {
        RLock lock = redissonClient.getLock(FULL_INDEX_LOCK);

        try {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
                log.info("[LOCK RELEASED] 전체 색인 락 해제 완료");
            } else {
                log.warn("해당 락은 현재 스레드가 보유 중이 아님");
            }
        } catch (Exception e) {
            log.error("전체 색인 락 해제 중 오류 발생", e);
        }
    }

    /**
     * 현재 락 점유 여부 확인
     */
    public boolean isLocked() {
        RLock lock = redissonClient.getLock(FULL_INDEX_LOCK);
        return lock.isLocked();
    }
}
