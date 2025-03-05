package com.example.ficketqueue.queue.enums;

public enum KeyType {

    FICKET_KAFKA_TOPIC("ficket-queue-%s"),
    FICKET_REDIS_QUEUE_KEY("ficket:queue:%s"),
    FICKET_WORK_SPACE_REDIS_KEY("ficket:workspace:%s:%s"),
    SLOT_ACTIVE_KEY("slots:active:%s"),
    SLOT_MAX_KEY("slots:max:%s");

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}
