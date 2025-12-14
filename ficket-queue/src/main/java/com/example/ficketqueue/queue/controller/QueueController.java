package com.example.ficketqueue.queue.controller;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.service.QueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queues")
public class QueueController {

    private final QueueService queueService;

    /**
     * 해당 공연에 대기열 진입 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-17
     * 변경 이력:
     * - 2024-12-17 오형상: 초기 작성
     * - 2025-12-11 오형상: 비동기 -> 동기
     */
    @GetMapping("/{eventId}/enter")
    public ResponseEntity<Void> enterQueue(@RequestHeader("X-User-Id") String userId,
                                           @PathVariable String eventId) {
        queueService.enterQueue(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 나의 대기열 상태 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-17
     * 변경 이력:
     * - 2024-12-17 오형상: 초기 작성
     * - 2025-12-11 오형상: 비동기 -> 동기
     */
    @GetMapping("/{eventId}/my-status")
    public ResponseEntity<MyQueueStatusResponse> getQueueStatus(@RequestHeader("X-User-Id") String userId,
                                                                @PathVariable String eventId) {
        return ResponseEntity.ok(queueService.getQueueStatus(userId, eventId));
    }


}
