package com.example.ficketsearch.global.config.redis;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 색인 락 관련 상수를 관리하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum IndexingLockConstants {
    
    // 락 키
    INDEXING_RW_LOCK("INDEXING_RW_LOCK", "Read-Write Lock 키"),
    
    // 전체 색인 락 타이밍
    FULL_INDEXING_WAIT_TIME_SEC("300", "전체 색인 락 대기 시간 (5분)"),
    FULL_INDEXING_LEASE_TIME_SEC("7200", "전체 색인 락 유지 시간 (2시간)"),
    
    // 부분 색인 락 타이밍
    PARTIAL_INDEXING_WAIT_TIME_SEC("3", "부분 색인 락 대기 시간 (3초)"),
    PARTIAL_INDEXING_LEASE_TIME_SEC("30", "부분 색인 락 유지 시간 (30초)");
    
    private final String value;
    private final String description;
    
    /**
     * String 값을 반환
     */
    @Override
    public String toString() {
        return value;
    }
    
    /**
     * long 값으로 변환 (시간 관련 상수용)
     */
    public long toLong() {
        return Long.parseLong(value);
    }
}