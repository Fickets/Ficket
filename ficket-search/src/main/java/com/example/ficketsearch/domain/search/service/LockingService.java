package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


@Slf4j
@Service
@RequiredArgsConstructor
public class LockingService {

    private final RedissonClient redisson;

    /**
     * 특정 락의 점유 상태를 확인합니다.
     *
     * @param lockName - 확인하려는 락의 이름
     * @return boolean - 락이 점유된 상태이면 true, 그렇지 않으면 false
     */
    public boolean isLockAcquired(String lockName) {
        RLock lock = redisson.getLock(lockName);
        boolean isLocked = lock.isLocked();
        log.info("락 점유 상태 확인 - 락 이름: {}, 점유 상태: {}", lockName, isLocked);
        return isLocked;
    }

    /**
     * 특정 락을 해제합니다.
     *
     * @param lockName - 해제하려는 락의 이름
     */
    public void releaseLock(String lockName) {
        RLock lock = redisson.getLock(lockName);
        if (lock.isHeldByCurrentThread()) {
            lock.unlock();
            log.info("락이 해제되었습니다 - 락 이름: {}", lockName);
        } else {
            log.warn("현재 스레드에서 소유하지 않은 락은 해제할 수 없습니다 - 락 이름: {}", lockName);
        }
    }

}
