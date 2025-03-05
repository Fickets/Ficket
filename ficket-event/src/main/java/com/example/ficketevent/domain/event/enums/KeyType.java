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
    EVENT_RESERVATION_RANKING("ficket:event:ranking:reserve:%s:%s"), // 예매율 랭킹

    // 좌석 수
    SEAT_TOTAL_COUNT("total_seat_count:%s"),
    ;

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}