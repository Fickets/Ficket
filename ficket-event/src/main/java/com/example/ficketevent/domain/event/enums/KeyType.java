package com.example.ficketevent.domain.event.enums;

public enum KeyType {
    // 좌석 선점
    SEAT_STATE("ficket:seats:%d"),
    USER_EVENT("ficket:user:%d:events"),
    SEAT_LOCK("seatLock:%d:%d"),

    // 캐시
    EVENT_DETAIL_CACHE("ficket:event:detail:%d"),

    // 랭킹
    EVENT_VIEW_RANKING("ficket:event:ranking:view"),         // 조회 수 기준 랭킹
    EVENT_RESERVATION_DAILY("ficket:event:ranking:reserve:daily"), // 일간 예매율 랭킹
    EVENT_RESERVATION_WEEKLY("ficket:event:ranking:reserve:weekly"), // 주간 예매율 랭킹
    EVENT_RESERVATION_MONTHLY("ficket:event:ranking:reserve:monthly"),
    ; // 월간 예매율 랭킹

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}