package com.example.ficketticketing.domain.order.controller;

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

}
