package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockingService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String INDEXING_LOCK = "FULL_INDEXING_LOCK";

    /**
     * 현재 인덱싱 락이 걸려있는지 확인합니다.
     *
     * @return boolean - 락이 걸려 있으면 true, 없으면 false
     */
    public boolean isLockAcquired() {
        Boolean exists = redisTemplate.hasKey(INDEXING_LOCK);
        log.info("인덱싱 락 상태 확인 - 존재 여부: {}", exists);
        return Boolean.TRUE.equals(exists);
    }

    /**
     * 인덱싱 락을 해제합니다.
     */
    public void releaseLock() {
        redisTemplate.delete(INDEXING_LOCK);
        log.info("인덱싱 락이 해제되었습니다.");
    }
}
