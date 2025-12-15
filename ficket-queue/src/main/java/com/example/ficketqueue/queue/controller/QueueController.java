package com.example.ficketqueue.queue.controller;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.service.QueueService;
import com.example.ficketqueue.queue.service.SlotService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/queues")
public class QueueController {

    private final QueueService queueService;
    private final SlotService slotService;

    /**
     * 해당 공연에 대기열 진입 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-17
     * 변경 이력:
     * - 2024-12-17 오형상: 초기 작성
     * - 2025-12-11 오형상: 비동기 -> 동기
     */
    @PostMapping("/{eventId}/enter-queue")
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

    /**
     * 대기열 퇴장 API
     *
     * 작업자: 오형상
     * 작업 날짜: 2025-12-15
     * 변경 이력:
     * - 2025-12-15 오형상: 초기 작성
     */
    @PostMapping("/{eventId}/leave-queue")
    public ResponseEntity<Void> leaveQueue(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String eventId
    ) {
        queueService.leaveQueue(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 예매 화면 입장 시도 API
     * - waitingAhead == 0 이 된 클라이언트가 호출
     * - 실제 입장 가능 여부는 Redis Lua에서 판단
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-12-14
     * 변경 이력:
     * - 2025-12-14 오형상: 초기 작성
     *
     */
    @PostMapping("/{eventId}/enter-ticketing")
    public ResponseEntity<Boolean> enterTicketing(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String eventId
    ) {
        boolean enter = queueService.enterTicketing(userId, eventId);

        return ResponseEntity.ok(enter);
    }

    /**
     * 예매 화면 퇴장 API
     * - 결제 완료 / 실패 / 이탈 시 호출
     * - 작업 슬롯 해제
     *
     * 작업자: 오형상
     * 작업 날짜: 2025-12-14
     * 변경 이력:
     * - 2025-12-14 오형상: 초기 작성
     */
    @PostMapping("/{eventId}/leave-ticketing")
    public ResponseEntity<Void> leaveTicketing(
            @RequestHeader("X-User-Id") String userId,
            @PathVariable String eventId
    ) {
        queueService.leaveTicketing(userId, eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 해당 이벤트의 슬롯 초기화 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     * - 2025-12-14 오형상: 비동기 -> 동기
     */
    @PostMapping("/{eventId}/initialize-slot")
    public ResponseEntity<Void> initializeSlot(@PathVariable String eventId, @RequestParam int maxSlot) {
        slotService.setMaxSlot(eventId, maxSlot);
        return ResponseEntity.noContent().build();
    }

//    /**
//     * 주문 상태 웹 소켓 전송 API
//     * <p>
//     * 작업자: 오형상
//     * 작업 날짜: 2024-12-21
//     * 변경 이력:
//     * - 2024-12-21 오형상: 초기 작성
//     */
//    @PostMapping("/{userId}/send-order-status")
//    public Mono<Void> sendOrderStatus(@PathVariable Long userId, @RequestParam("orderStatus") String orderStatus) {
//        if (orderStatus.equals("Paid")) {
//            return clientNotificationService.notifyUser(String.valueOf(userId), WorkStatus.ORDER_PAID);
//        } else if (orderStatus.equals("Failed")) {
//            return clientNotificationService.notifyUser(String.valueOf(userId), WorkStatus.ORDER_FAILED);
//        }
//        return Mono.empty();
//    }

}
