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
        queueRepository.enterQueue(userId, eventId);
    }

    @Override
    public void leaveQueue(String userId, String eventId) {
        Long result = queueRepository.leaveQueue(userId, eventId);

        if (result == null || result == 0) {
            throw new IllegalStateException("대기열에서 나가기 실패");
        }
    }

    @Override
    public MyQueueStatusResponse getQueueStatus(String userId, String eventId) {
        return queueRepository.getQueueStatus(userId, eventId);
    }

    @Override
    public boolean enterTicketing(String userId, String eventId) {
        Long enter = queueRepository.enterTicketing(userId, eventId);
        return enter == 1L;
    }

    @Override
    public void leaveTicketing(String userId, String eventId) {
        queueRepository.leaveTicketing(userId, eventId);
    }

    @Override
    public boolean isInTicketing(String userId, String eventId) {
        return queueRepository.existsWorkingUser(userId, eventId);
    }
}
