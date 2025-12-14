package com.example.ficketqueue.global.utils;

public enum KeyType {

    NEXT_NUMBER("queue:%s:nextNumber"),           // 다음 순번 발급
    CURRENT_NUMBER("queue:%s:currentNumber"),     // 현재 예매 화면 접속 인원
    USER_QUEUE_NUMBER("queue:%s:user:%s"),       // 사용자별 순번
    WORKING_USER("queue:%s:working:%s"),         // 예매 화면 접속 사용자, TTL 관리
    MAX_CONCURRENT("queue:%s:maxConcurrent");    // 이벤트별 최대 동시 접속자
//    LOCK("queue:%s:lock");

    private final String keyPattern;

    KeyType(String keyPattern) {
        this.keyPattern = keyPattern;
    }

    public String format(Object... args) {
        return String.format(keyPattern, args);
    }
}