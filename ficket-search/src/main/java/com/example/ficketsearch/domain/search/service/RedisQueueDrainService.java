package com.example.ficketsearch.domain.search.service;

import com.example.ficketsearch.domain.search.messageQueue.PartialIndexingConsumer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisQueueDrainService {

    private final RedisQueueService redisQueueService;
    private final PartialIndexingConsumer consumer;

    public void drainQueue() {
        String msg;
        while ((msg = redisQueueService.dequeue()) != null) {
            consumer.handlePartialIndexing(msg); 
        }
    }
}