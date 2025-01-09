package com.example.ficketticketing.domain.order.controller;

import com.example.ficketticketing.domain.order.dto.client.*;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse;
import com.example.ficketticketing.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticketing/order")
public class OrderController {

    private final OrderService orderService;

    /**
     * 주문 생성 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-07
     * 변경 이력:
     * - 2024-12-07 오형상: 초기 작성
     * - 2024-12-08 오형상: 주문 검증 로직 추가
     * - 2024-12-15 오형상: 주문 실패시 상태 원복 로직 및 예매율 랭킹 로직 추가
     * - 2024-12-22 오형상: 얼굴 등록 api 분리 및 얼굴 의존성 추가 로직 추가
     */
    @PostMapping
    public ResponseEntity<Long> createOrder(@RequestBody CreateOrderRequest createOrderRequest, @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(orderService.createOrder(createOrderRequest, Long.parseLong(userId)));
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


    /**
     * 해당 유저 예매 티켓 정보 전체 조회
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 최용수: 초기 작성
     */
    @GetMapping("/customer")
    public List<OrderInfoDto> getCustomerTicket(@RequestParam Long userId){
        return orderService.getCustomerTickets(userId);
    }

    /**
     * 티켓팅 얼굴 등록
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-22
     * 변경 이력:
     * - 2024-12-22 오형상: 초기 작성
     */
    @PostMapping("{eventScheduleId}/upload-face")
    public ResponseEntity<FaceApiResponse> uploadUserFace(@RequestPart MultipartFile userImg, @PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(orderService.uploadUserFace(userImg, eventScheduleId));
    }

    /**
     * 티켓팅 얼굴 확인
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2025-01-03
     * 변경 이력:
     * - 2024-12-22 최용수: 초기 작성
     */
    @PostMapping("{eventScheduleId}/{connectId}/user-match")
    public ResponseEntity<Void> userMatch(@RequestPart MultipartFile userImg, @PathVariable Long eventId, @PathVariable Long connectId) {
        orderService.matchFace(userImg,eventId, connectId);
        return ResponseEntity.noContent().build();
    }
}
