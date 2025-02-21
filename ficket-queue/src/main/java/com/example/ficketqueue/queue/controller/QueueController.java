package com.example.ficketqueue.queue.controller;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.enums.WorkStatus;
import com.example.ficketqueue.queue.service.ClientNotificationService;
import com.example.ficketqueue.queue.service.QueueService;
import com.example.ficketqueue.queue.service.SlotService;
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
    private final SlotService slotService;
    private final ClientNotificationService clientNotificationService;

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
    @GetMapping("/{eventId}/my-status")
    public Mono<MyQueueStatusResponse> getQueueStatus(@RequestHeader("X-User-Id") String userId,
                                                      @PathVariable String eventId) {
        return queueService.getMyQueueStatus(userId, eventId);
    }

    /**
     * 대기열 없이 즉시 입장 가능 여부 확인 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/can-enter")
    public Mono<Boolean> canEnterImmediately(@PathVariable String eventId) {
        return Mono.zip(
                queueService.isQueueEmpty(eventId),
                slotService.hasAvailableSlot(eventId)
        ).map(tuple -> tuple.getT1() && tuple.getT2()); // 두 조건을 결합
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

    /**
     * 해당 이벤트의 슬롯 초기화 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     */
    @PostMapping("/{eventId}/initialize-slot")
    public Mono<Void> initializeSlot(@PathVariable String eventId, @RequestParam int maxSlot) {
        return slotService.setMaxSlot(eventId, maxSlot);
    }

    /**
     * 해당 이벤트의 슬롯 제거 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-24
     * 변경 이력:
     * - 2025-01-24 오형상: 초기 작성
     */
    @DeleteMapping("/{eventId}/delete-slot")
    public Mono<Void> removeSlot(@PathVariable String eventId) {
        return slotService.deleteSlot(eventId);
    }

    /**
     * 대기열 없이 즉시 입장 가능하면 슬롯 사용  API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     */
    @PostMapping("/{eventId}/occupy-slot")
    public Mono<Boolean> occupySlot(@RequestHeader("X-User-Id") String userId, @PathVariable String eventId) {
        return slotService.occupySlot(userId, eventId);
    }

    /**
     * 해당 이벤트의 슬롯 해체 API 
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     * - 2025-02-21 오형상: eventId 인수 추가 및 함수 수정
     */
    @DeleteMapping("/{eventId}/release-slot")
    public Mono<Void> releaseSlot(@PathVariable String eventId, @RequestHeader("X-User-Id") String userId) {
        return slotService.releaseSlotByEventIdAndUserId(eventId, userId);
    }

    /**
     * 주문 상태 웹 소켓 전송 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-21
     * 변경 이력:
     * - 2024-12-21 오형상: 초기 작성
     */
    @PostMapping("/{userId}/send-order-status")
    public Mono<Void> sendOrderStatus(@PathVariable Long userId, @RequestParam("orderStatus") String orderStatus) {
        if (orderStatus.equals("Paid")) {
            return clientNotificationService.notifyUser(String.valueOf(userId), WorkStatus.ORDER_PAID);
        } else if (orderStatus.equals("Failed")) {
            return clientNotificationService.notifyUser(String.valueOf(userId), WorkStatus.ORDER_FAILED);
        }
        return Mono.empty();
    }
}
