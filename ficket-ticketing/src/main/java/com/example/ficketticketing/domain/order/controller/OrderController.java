package com.example.ficketticketing.domain.order.controller;

import com.example.ficketticketing.domain.order.dto.client.DailyRevenueResponse;
import com.example.ficketticketing.domain.order.dto.client.DayCountResponse;
import com.example.ficketticketing.domain.order.dto.client.TicketInfoDto;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse;
import com.example.ficketticketing.domain.order.service.OrderService;
import com.example.ficketticketing.domain.order.service.PaymentSseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Set;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticketing/order")
public class OrderController {

    private final OrderService orderService;
    private final PaymentSseService paymentSseService;

    /**
     * 주문 생성 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-07
     * 변경 이력:
     * - 2024-12-07 오형상: 초기 작성
     * - 2024-12-08 오형상: 주문 검증 로직 추가
     * - 2024-12-15 오형상: 주문 실패시 상태 원복 로직 및 예매율 랭킹 로직 추가
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestPart CreateOrderRequest createOrderRequest, @RequestPart MultipartFile userFaceImg, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.createOrder(createOrderRequest, userFaceImg, Long.parseLong(userId)));
    }

    /**
     * 주문 상태 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-07
     * 변경 이력:
     * - 2024-12-07 오형상: 초기 작성
     * - 2024-12-08 오형상: 반환값 수정
     */
    @GetMapping("/{orderId}")
    public ResponseEntity<OrderStatusResponse> getOrderStatus(@PathVariable Long orderId) {
        return ResponseEntity.ok(orderService.getOrderStatus(orderId));
    }

    /**
     * 포트원 웹훅 처리 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-06
     * 변경 이력:
     * - 2024-12-06 오형상: 초기 작성
     */
    @PostMapping("/valid")
    public ResponseEntity<String> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("webhook-id") String webhookId,
            @RequestHeader("webhook-signature") String webhookSignature,
            @RequestHeader("webhook-timestamp") String webhookTimestamp
    ) {
        orderService.processWebhook(webhookId, webhookSignature, webhookTimestamp, payload);
        return ResponseEntity.ok("Webhook processed");
    }

    /**
     * 클라이언트에서 포트원 웹훅 결과 SSE 받기 위해 구독 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-06
     * 변경 이력:
     * - 2024-12-06 오형상: 초기 작성
     */
    @GetMapping("/subscribe/{paymentId}")
    public SseEmitter subscribeToPaymentStatus(@PathVariable String paymentId) {
        return paymentSseService.subscribe(paymentId);
    }

    /**
     * 주문(티켓) 취소 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-09
     * 변경 이력:
     * - 2024-12-09 오형상: 초기 작성
     */
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        orderService.refundOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 마이 티켓 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-09
     * 변경 이력:
     * - 2024-12-09 오형상: 초기 작성
     */
    @GetMapping("/my")
    public ResponseEntity<List<TicketInfoDto>> getMyTickets(@RequestParam Long userId) {
        return ResponseEntity.ok(orderService.getMyTickets(userId));
    }

    @PostMapping("/all-user-id")
    public int[] getTicketingUserIdList(@RequestBody List<Long> scheduleId){
        int[] res = orderService.getTicketUserStatistic(scheduleId);
        return res;
    }

    /**
     * 날짜별 수익 (event -> ticketing) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/daily-revenue")
    public ResponseEntity<List<DailyRevenueResponse>> calculateDailyRevenue(@RequestParam("ticketIds") Set<Long> ticketIds){
        return ResponseEntity.ok(orderService.calculateDailyRevenue(ticketIds));
    }

    /**
     * 요일별 예매수 (event -> ticketing) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/day-count")
    public ResponseEntity<DayCountResponse> calculateDayCount(@RequestParam("ticketIds") Set<Long> ticketIds){
        return ResponseEntity.ok(orderService.calculateDayCount(ticketIds));
    }

    /**
     * 입장 제한을 위한 해당 회원의 해당 회차 구매 가능 티켓 수 반환 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-11
     * 변경 이력:
     * - 2024-12-11 오형상: 초기 작성
     */
    @GetMapping("/enter-ticketing/{eventScheduleId}")
    public ResponseEntity<Integer> enterTicketing(@RequestHeader("X-User-Id") String userId, @PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(orderService.enterTicketing(Long.parseLong(userId), eventScheduleId));
    }
}
