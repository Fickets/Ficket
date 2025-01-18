package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LockingService {

    private final RedissonClient redissonClient;
    private final RedisTemplate<String, String> redisTemplate;

    /**
     * 락을 요청하고, TTL을 설정하여 작업을 실행하는 메서드
     *
     * @param lockName - 락 이름
     * @param ttl      - 락의 TTL (밀리초 단위)
     * @param action   - 락이 얻어졌을 때 실행할 작업
     * @throws InterruptedException - 락 획득 중 인터럽트 예외
     */
    public void executeWithLock(String lockName, long ttl, Runnable action) throws InterruptedException {
        RLock lock = redissonClient.getLock(lockName);

        try {
            // TTL을 설정하여 락을 얻을 때까지 대기하고, TTL이 만료되면 자동 해제
            lock.lock(ttl, TimeUnit.MILLISECONDS);
            // 락을 얻은 후 실제 작업 실행
            action.run();
        } finally {
            // 작업이 끝난 후 락 해제
            lock.unlock();
        }
    }

    /**
     * 특정 키(플래그)를 Redis에서 해제하는 메서드
     *
     * @param key - Redis 키
     */
    public void release(String key) {
        Boolean deleted = redisTemplate.delete(key);
        if (Boolean.TRUE.equals(deleted)) {
            log.info("플래그 해제 완료: {}", key);
        } else {
            log.warn("플래그 해제 실패 또는 이미 존재하지 않음: {}", key);
        }
    }

    /**
     * 특정 키(플래그)가 존재하는지 확인하는 메서드
     *
     * @param key - Redis 키
     * @return 존재 여부 (true/false)
     */
    public boolean isExist(String key) {
        Boolean exists = redisTemplate.hasKey(key);
        boolean result = exists != null && exists;
        log.info("키 존재 여부 확인: {}, 결과: {}", key, result);
        return result;
    }
}
