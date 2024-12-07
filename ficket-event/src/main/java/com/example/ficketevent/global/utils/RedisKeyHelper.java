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
}