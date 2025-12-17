package com.example.ficketsearch.global.config.awsS3;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.format.DateTimeFormatter;

/**
 * S3 관련 상수를 관리하는 Enum
 */
@Getter
@RequiredArgsConstructor
public enum S3Constants {
    
    BUCKET_NAME("ficket-event-content", "S3 버킷 이름"),
    SNAPSHOT_BASE_PATH("elasticsearch/snapshot", "스냅샷 저장 경로"),
    
    // 전체 색인 관련
    FULL_INDEX_PREFIX("index/full/", "전체 색인 파일 경로 prefix"),
    SUCCESS_FILE("_SUCCESS", "데이터 준비 완료 표시 파일"),
    CSV_EXTENSION(".csv", "CSV 파일 확장자"),
    
    // 날짜 포맷
    DATE_FORMAT("yyyy/MM/dd", "S3 경로 날짜 포맷");
    
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
     * DateTimeFormatter 반환 (DATE_FORMAT용)
     */
    public DateTimeFormatter toDateFormatter() {
        return DateTimeFormatter.ofPattern(value);
    }
    
    /**
     * S3 URI 생성
     */
    public static String buildS3Uri(String key) {
        return String.format("s3://%s/%s", BUCKET_NAME.value, key);
    }
    
    /**
     * 전체 색인 경로 생성 (날짜 포함)
     */
    public static String buildFullIndexPath(String datePath) {
        return FULL_INDEX_PREFIX.value + datePath + "/";
    }
}