package com.example.ficketqueue.global.utils;

/**
 * Redis Key 생성 헬퍼
 * KeyType enum과 함께 사용하여 Redis Key 생성
 */
public class KeyHelper {

    private KeyHelper() {
        // Utility 클래스 생성 방지
    }

    /**
     * 이벤트별 nextNumber Key 생성
     */
    public static String nextNumberKey(String eventId) {
        return KeyType.NEXT_NUMBER.format(eventId);
    }

    /**
     * 이벤트별 currentNumber Key 생성
     */
    public static String currentNumberKey(String eventId) {
        return KeyType.CURRENT_NUMBER.format(eventId);
    }

    /**
     * 이벤트별 대기열 ZSET Key 생성
     */
    public static String waitingZSetKey(String eventId) {
        return KeyType.WAITING_ZSET.format(eventId);
    }

    /**
     * 이벤트 + 사용자별 workingUser Key 생성
     */
    public static String workingUserKey(String eventId, String userId) {
        return KeyType.WORKING_USER.format(eventId, userId);
    }

    /**
     * 이벤트별 maxConcurrent Key 생성
     */
    public static String maxConcurrentKey(String eventId) {
        return KeyType.MAX_CONCURRENT.format(eventId);
    }
}
