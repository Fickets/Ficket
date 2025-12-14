package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;

public interface QueueService {

    void enterQueue(String userId, String eventId);

    MyQueueStatusResponse getQueueStatus(String userId, String eventId);

    boolean enterTicketing(String userId, String eventId);

    void leaveTicketing(String userId, String eventId);
}
