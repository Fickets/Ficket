package com.example.ficketsearch.global.config.elasticsearch;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Elasticsearch 관련 상수를 관리하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum ElasticsearchConstants {
    
    INDEX_NAME("event-data", "메인 인덱스 이름"),
    ALIAS_NAME("current", "인덱스 별칭"),
    
    // 스냅샷 관련
    SNAPSHOT_STORAGE_NAME("snapshot_storage", "스냅샷 저장소 이름"),
    SNAPSHOT_NAME("snapshot_latest", "스냅샷 이름"),
    
    // Bulk 설정
    BULK_SIZE("2000", "벌크 작업 배치 크기"),
    
    // 자동완성 설정
    AUTOCOMPLETE_SIZE("5", "자동완성 결과 최대 개수"),
    
    // 검색 설정
    TRACK_TOTAL_HITS_LIMIT("500000", "총 결과 수 추적 제한");
    
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
     * int 값으로 변환
     */
    public int toInt() {
        return Integer.parseInt(value);
    }
    
    /**
     * long 값으로 변환
     */
    public long toLong() {
        return Long.parseLong(value);
    }
}