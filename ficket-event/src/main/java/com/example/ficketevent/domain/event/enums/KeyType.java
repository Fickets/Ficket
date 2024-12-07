package com.example.ficketevent.domain.event.enums;

public enum KeyType {
    SEAT_STATE("ficket:seats:%d"),
    USER_EVENT("ficket:user:%d:events"),
    SEAT_LOCK("seatLock:%d:%d");

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}