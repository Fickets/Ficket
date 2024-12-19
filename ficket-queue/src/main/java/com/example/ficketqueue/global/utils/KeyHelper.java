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

}