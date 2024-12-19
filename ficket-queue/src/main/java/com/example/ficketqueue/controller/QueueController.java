package com.example.ficketqueue.controller;

import com.example.ficketqueue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;


@Slf4j
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
     */
    @GetMapping("/{eventId}/enter")
    public Mono<Void> enterQueue(@RequestHeader("X-User-Id") String userId,
                                   @PathVariable String eventId) {
        return queueService.enterQueue(userId, eventId);
    }

    /**
     * 나의 대기열 상태 조회 (초기 & 백업) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-17
     * 변경 이력:
     * - 2024-12-17 오형상: 초기 작성
     */
    @GetMapping("{eventId}/my-status")
    public Mono<MyQueueStatusResponse> getQueueStatus(@RequestHeader("X-User-Id") String userId,
                                                      @PathVariable String eventId) {
        return queueService.getMyQueueStatus(userId, eventId);
    }

    /**
     * 대기열 나가기 (백업용) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-18
     * 변경 이력:
     * - 2024-12-18 오형상: 초기 작성
     */
    @DeleteMapping("/{eventId}/leave")
    public Mono<Void> leaveQueue(@RequestHeader("X-User-Id") String userId,
                                 @PathVariable String eventId) {
        return queueService.leaveQueue(userId, eventId);
    }

}
