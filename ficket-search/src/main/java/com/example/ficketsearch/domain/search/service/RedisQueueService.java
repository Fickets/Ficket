package com.example.ficketsearch.domain.search.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RedisQueueService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String QUEUE_KEY = "partial_index_queue";

    public void enqueue(String message) {
        redisTemplate.opsForList().rightPush(QUEUE_KEY, message);
    }

    public String dequeue() {
        return redisTemplate.opsForList().leftPop(QUEUE_KEY);
    }

    public long size() {
        return redisTemplate.opsForList().size(QUEUE_KEY);
    }
}
