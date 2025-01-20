package com.example.ficketqueue.global.utils;


import com.example.ficketqueue.queue.enums.KeyType;

public class KeyHelper {

    private KeyHelper() {
        // Prevent instantiation
    }

    public static String getFicketKafkaQueue(String eventId) {
        return KeyType.FICKET_KAFKA_TOPIC.format(eventId);
    }

    public static String getFicketRedisQueue(String eventId) {
        return KeyType.FICKET_REDIS_QUEUE_KEY.format(eventId);
    }

    public static String getFicketWorkSpace(String eventId, String userId) {
        return KeyType.FICKET_WORK_SPACE_REDIS_KEY.format(eventId, userId);
    }

    public static String getActiveSlotKey(String eventId) {
        return KeyType.SLOT_ACTIVE_KEY.format(eventId);
    }

    public static String getMaxSlotKey(String eventId) {
        return KeyType.SLOT_MAX_KEY.format(eventId);
    }

}