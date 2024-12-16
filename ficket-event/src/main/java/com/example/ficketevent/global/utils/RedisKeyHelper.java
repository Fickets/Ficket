package com.example.ficketevent.global.utils;

import com.example.ficketevent.domain.event.enums.KeyType;

public class RedisKeyHelper {

    private RedisKeyHelper() {
        // Prevent instantiation
    }

    public static String getSeatKey(Long eventScheduleId) {
        return KeyType.SEAT_STATE.format(eventScheduleId);
    }

    public static String getUserKey(Long userId) {
        return KeyType.USER_EVENT.format(userId);
    }

    public static String getLockKey(Long eventScheduleId, Long seatMappingId) {
        return KeyType.SEAT_LOCK.format(eventScheduleId, seatMappingId);
    }

    public static String getEventDetailCacheKey(Long eventId) {
        return KeyType.EVENT_DETAIL_CACHE.format(eventId);
    }

    public static String getViewRankingKey() {
        return KeyType.EVENT_VIEW_RANKING.format();
    }

    public static String getReservationKey(String period, String genre) {
        return KeyType.EVENT_RESERVATION_RANKING.format(period, genre);
    }

}