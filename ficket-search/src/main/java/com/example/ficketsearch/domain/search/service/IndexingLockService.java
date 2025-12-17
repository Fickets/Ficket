package com.example.ficketsearch.domain.search.service;

import com.example.ficketsearch.global.config.redis.IndexingLockConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RReadWriteLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingLockService {

    private final RedissonClient redissonClient;
    /**
     * 부분 색인용 Read Lock 획득
     * - 여러 부분 색인이 동시에 진행 가능
     * - 전체 색인(Write Lock)이 대기 중이거나 진행 중이면 획득 실패
     */
    public boolean acquirePartialIndexingLock(long waitTimeSec, long leaseTimeSec) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(IndexingLockConstants.INDEXING_RW_LOCK.toString());
        RLock readLock = rwLock.readLock();

        try {
            boolean acquired = readLock.tryLock(waitTimeSec, leaseTimeSec, TimeUnit.SECONDS);
            
            if (acquired) {
                log.info("[READ LOCK ACQUIRED] 부분 색인 락 획득 성공");
            } else {
                log.info("[READ LOCK FAILED] 전체 색인 진행 중 또는 대기 중");
            }
            
            return acquired;
            
        } catch (InterruptedException e) {
            log.error("부분 색인 락 획득 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("부분 색인 락 획득 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 부분 색인용 Read Lock 해제
     */
    public void releasePartialIndexingLock() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(IndexingLockConstants.INDEXING_RW_LOCK.toString());
        RLock readLock = rwLock.readLock();

        try {
            if (readLock.isHeldByCurrentThread()) {
                readLock.unlock();
                log.info("[READ LOCK RELEASED] 부분 색인 락 해제 완료");
            } else {
                log.warn("해당 Read Lock은 현재 스레드가 보유 중이 아님");
            }
        } catch (Exception e) {
            log.error("부분 색인 락 해제 중 오류 발생", e);
        }
    }

    /**
     * 전체 색인용 Write Lock 획득
     * - 모든 부분 색인(Read Lock)이 해제될 때까지 자동 대기
     * - Write Lock 획득 후에는 새로운 부분 색인 시작 불가
     */
    public boolean acquireFullIndexingLock(long waitTimeSec, long leaseTimeSec) {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(IndexingLockConstants.INDEXING_RW_LOCK.toString());
        RLock writeLock = rwLock.writeLock();

        try {
            log.info("[WRITE LOCK] 전체 색인 락 획득 시도 (진행 중인 부분 색인 대기 중...)");
            
            boolean acquired = writeLock.tryLock(waitTimeSec, leaseTimeSec, TimeUnit.SECONDS);
            
            if (acquired) {
                log.info("[WRITE LOCK ACQUIRED] 전체 색인 락 획득 성공 (모든 부분 색인 완료됨)");
            } else {
                log.warn("[WRITE LOCK FAILED] 전체 색인 락 획득 실패 (타임아웃 또는 다른 서버에서 진행 중)");
            }
            
            return acquired;
            
        } catch (InterruptedException e) {
            log.error("전체 색인 락 획득 중 인터럽트 발생", e);
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            log.error("전체 색인 락 획득 중 오류 발생", e);
            return false;
        }
    }

    /**
     * 전체 색인용 Write Lock 해제
     */
    public void releaseFullIndexingLock() {
        RReadWriteLock rwLock = redissonClient.getReadWriteLock(IndexingLockConstants.INDEXING_RW_LOCK.toString());
        RLock writeLock = rwLock.writeLock();

        try {
            if (writeLock.isHeldByCurrentThread()) {
                writeLock.unlock();
                log.info("[WRITE LOCK RELEASED] 전체 색인 락 해제 완료");
            } else {
                log.warn("해당 Write Lock은 현재 스레드가 보유 중이 아님");
            }
        } catch (Exception e) {
            log.error("전체 색인 락 해제 중 오류 발생", e);
        }
    }

}