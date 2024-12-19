package com.example.ficketqueue.enums;

public enum KeyType {

    FICKET_KAFKA_TOPIC("ficket-queue-%s"),
    FICKET_REDIS_QUEUE_KEY("ficket:queue:%s"),
    ;

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}
