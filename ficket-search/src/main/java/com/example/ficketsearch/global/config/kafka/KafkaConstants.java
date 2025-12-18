package com.example.ficketsearch.global.config.kafka;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Kafka 관련 상수를 관리하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum KafkaConstants {
    
    // 토픽
    PARTIAL_INDEXING_TOPIC("partial-indexing", "부분 색인 토픽"),
    INDEXING_CONTROL_TOPIC("indexing-control", "색인 제어 토픽"),
    
    // Consumer Group
    PARTIAL_INDEXING_GROUP("partial-indexing-group", "부분 색인 Consumer Group"),
    INDEXING_CONTROL_GROUP("indexing-control-group", "색인 제어 Consumer Group"),
    
    // Listener ID
    PARTIAL_INDEXING_LISTENER("partialIndexingListener", "부분 색인 Listener ID"),
    
    // 메시지
    FULL_INDEXING_STARTED("FULL_INDEXING_STARTED", "전체 색인 시작 메시지"),
    FULL_INDEXING_FINISHED("FULL_INDEXING_FINISHED", "전체 색인 완료 메시지")
    ;
    
    private final String value;
    private final String description;
    
    /**
     * String 값을 반환
     */
    @Override
    public String toString() {
        return value;
    }
}