package com.example.ficketqueue.queue.repository;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;

public interface QueueRepository {

    /**
     * 대기열 진입 (순번 발급)
     */
    Long enterQueue(String userId, String eventId);

    Long leaveQueue(String userId, String eventId);

    /**
     * 예매 화면 진입 허용 (currentNumber 증가 + TTL)
     *
     * @return 1 = 입장 성공, 0 = 입장 불가
     */
    Long enterTicketing(String userId, String eventId);

    /**
     * 예매 화면 퇴장 시 currentNumber 감소
     *
     * @return 1 = 감소 성공, 0 = 이미 0
     */
    Long leaveTicketing(String userId, String eventId);

    MyQueueStatusResponse getQueueStatus(String userId, String eventId);

    boolean existsWorkingUser(String userId, String eventId);

    void decrementCurrentNumber(String eventId);
}