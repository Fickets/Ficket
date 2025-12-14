package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.repository.QueueRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class QueueServiceImpl implements QueueService {

    private final QueueRepository queueRepository;

    @Override
    public void enterQueue(String userId, String eventId) {
        Long myNumber = queueRepository.enterQueue(userId, eventId);

    }

    @Override
    public MyQueueStatusResponse getQueueStatus(String userId, String eventId) {
        return null;
    }
}
