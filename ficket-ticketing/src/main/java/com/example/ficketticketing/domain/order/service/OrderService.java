package com.example.ficketticketing.domain.order.service;

import com.example.ficketticketing.domain.check.dto.CheckDto;
import com.example.ficketticketing.domain.check.service.CheckService;
import com.example.ficketticketing.domain.order.client.*;
import com.example.ficketticketing.domain.order.dto.client.*;
import com.example.ficketticketing.domain.order.dto.kafka.OrderDto;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.request.SelectSeatInfo;
import com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDto;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDtoList;
import com.example.ficketticketing.domain.order.entity.*;
import com.example.ficketticketing.domain.order.mapper.OrderMapper;
import com.example.ficketticketing.domain.order.mapper.TicketMapper;
import com.example.ficketticketing.domain.order.messagequeue.OrderProducer;
import com.example.ficketticketing.domain.order.repository.OrderRepository;
import com.example.ficketticketing.domain.order.repository.TicketRepository;
import com.example.ficketticketing.domain.order.repository.RefundPolicyRepository;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.example.ficketticketing.infrastructure.payment.PortOneApiClient;
import com.example.ficketticketing.infrastructure.payment.dto.WebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static com.example.ficketticketing.global.utils.CircuitBreakerUtils.executeWithCircuitBreaker;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class OrderService {

    @Value("${portone.webhook.secret}")
    private String WEBHOOK_SECRET;

    private static final long WEBHOOK_TOLERANCE_IN_SECONDS = 5 * 60L;
    private final Map<String, SecretKeySpec> secretKeyCache = new ConcurrentHashMap<>();

    private final PortOneApiClient portOneApiClient;
    private final OrderRepository orderRepository;
    private final TicketRepository ticketRepository;
    private final RefundPolicyRepository refundPolicyRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final FaceServiceClient faceServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final OrderProducer orderProducer;
    private final TicketMapper ticketMapper;
    private final QueueServiceClient queueServiceClient;
    private final OrderMapper orderMapper;
    private final AdminServiceClient adminServiceClient;
    private final CheckService checkService;

    public void processWebhook(String webhookId, String webhookSignature, String webhookTimestamp, String payload) {
        // 1. 타임스탬프 검증
        verifyTimestamp(webhookTimestamp);

        // 2. 시그니처 검증
        String expectedSignature = generateSignature(webhookId, webhookTimestamp, payload);
        if (!verifySignature(expectedSignature, webhookSignature)) {
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);
        }

        // 3. JSON 데이터 파싱
        WebhookPayload data = parseJson(payload);

        // 4. 이벤트 처리 로직
        handleEvent(data);
    }

    private void verifyTimestamp(String timestampHeader) {
        long now = System.currentTimeMillis() / 1000;
        long timestamp = Long.parseLong(timestampHeader);

        if (Math.abs(now - timestamp) > WEBHOOK_TOLERANCE_IN_SECONDS) {
            throw new BusinessException(ErrorCode.TIMESTAMP_TOO_OLD_OR_NEW);
        }
    }

    private String generateSignature(String webhookId, String webhookTimestamp, String payload) {
        String dataToSign = String.format("%s.%s.%s", webhookId, webhookTimestamp, payload);

        SecretKeySpec secretKey = secretKeyCache.computeIfAbsent(WEBHOOK_SECRET, this::createSecretKeySpec);

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(secretKey);
            byte[] hmacBytes = mac.doFinal(dataToSign.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            log.error("Error generating signature", e);
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);
        }
    }

    private SecretKeySpec createSecretKeySpec(String secret) {
        try {
            String trimmedSecret = secret.startsWith("whsec_") ? secret.substring("whsec_".length()) : secret;
            byte[] decodedSecret = Base64.getDecoder().decode(trimmedSecret);
            return new SecretKeySpec(decodedSecret, "HmacSHA256");
        } catch (Exception e) {
            log.error("Error creating SecretKeySpec", e);
            throw new BusinessException(ErrorCode.INVALID_SIGNATURE);
        }
    }

    private boolean verifySignature(String expectedSignature, String webhookSignature) {
        String[] signatureParts = webhookSignature.split(" ");
        for (String part : signatureParts) {
            String[] split = part.split(",", 2);
            if (split.length == 2 && "v1".equals(split[0])) {
                String signature = split[1];
                if (expectedSignature.equals(signature)) {
                    return true;
                }
            }
        }
        return false;
    }

    private WebhookPayload parseJson(String json) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(json, WebhookPayload.class);
        } catch (Exception e) {
            log.error("Error parsing JSON", e);
            throw new BusinessException(ErrorCode.INVALID_JSON_FORMAT);
        }
    }

    private void handleEvent(WebhookPayload data) {
        String type = data.getType();
        String paymentId = data.getData().getPaymentId();

        log.info("Processing event type: {}", type);
        switch (type) {
            case "Transaction.Ready":
                log.info("결제창 오픈");
                break;
            case "Transaction.Paid":
                if (verifyPaidInfo(paymentId)) {

                    try {
                        orderRepository.updateOrderStatusToCompleted(paymentId);

                        Orders order = orderRepository.findByPaymentId(paymentId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));

                        // create settlement
                        List<Long> ids = executeWithCircuitBreaker(
                                circuitBreakerRegistry,
                                "getCompanyEventIdByTicketId",
                                () -> eventServiceClient.getCompanyEventId(order.getTicket().getTicketId()));

                        OrderSimpleDto orderSimpleDto = orderMapper.toOrderSimpleDto(order);
                        orderSimpleDto.setCompanyId(ids.get(0));
                        orderSimpleDto.setEventId(ids.get(1));

                        ResponseEntity<Void> settlementCreateByOrder = executeWithCircuitBreaker(
                                circuitBreakerRegistry,
                                "settlementCreateByOrder",
                                () -> adminServiceClient.createSettlement(orderSimpleDto)
                        );

                        log.info("settlement created success");

                        notifyClient(paymentId, "Paid");
                    } catch (Exception e) {
                        portOneApiClient.cancelOrder(paymentId);
                    }

                } else {
                    portOneApiClient.cancelOrder(paymentId);
                }
                break;
            case "Transaction.Cancelled":
                Long ticketIdByPaymentId = orderRepository.findTicketIdByPaymentId(paymentId);
                orderRepository.cancelByPaymentId(paymentId);
                ticketRepository.deleteByTicketId(ticketIdByPaymentId);
                executeWithCircuitBreaker(
                        circuitBreakerRegistry,
                        "deleteFaceCircuitBreaker",
                        () -> faceServiceClient.deleteFace(ticketIdByPaymentId)
                );
                notifyClient(paymentId, "Failed");
                break;
            default:
                log.warn("알 수 없는 이벤트 타입: {}", type);
                throw new BusinessException(ErrorCode.INVALID_EVENT_TYPE);
        }
    }

    public Long createOrder(CreateOrderRequest createOrderRequest, Long userId) {
        // 유저 조회
        UserSimpleDto userDto = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getUserCircuitBreaker",
                () -> userServiceClient.getUser(userId)
        );

        ValidSeatInfoResponse validSeatInfoResponse = checkRequestValid(createOrderRequest);

        Set<Long> seatMappingIds = createOrderRequest.getSelectSeatInfoList().stream().map(SelectSeatInfo::getSeatMappingId)
                .collect(Collectors.toSet());

        if (seatMappingIds.isEmpty() || seatMappingIds.size() > validSeatInfoResponse.getReservationLimit()) {
            throw new BusinessException(ErrorCode.INPUT_VALUE_INVALID);
        }

        checkSeatReservation(userDto.getUserId(), createOrderRequest.getEventScheduleId(), seatMappingIds);

        Orders createdOrder = Orders.createOrder(createOrderRequest, userDto.getUserId());

        orderRepository.save(createdOrder);

        FaceApiResponse faceApiResponse = executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "settingRelationshipCircuitBreaker",
                () -> faceServiceClient.settingRelationship(ticketMapper.toUploadFaceInfo(createOrderRequest, createdOrder.getTicket().getTicketId()))
        );

        log.info(faceApiResponse.getMessage());

        orderProducer.send("order-events", new OrderDto(createOrderRequest.getEventScheduleId(), createdOrder.getOrderId(), seatMappingIds, createdOrder.getTicket().getTicketId()));

        return createdOrder.getOrderId();
    }

    @Transactional(readOnly = true)
    public OrderStatusResponse getOrderStatus(Long orderId) {
        return orderRepository.findOrderStatusByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_STATUS));
    }


    private void checkSeatReservation(Long userId, Long eventScheduleId, Set<Long> seatMappingIds) {
        ReservedSeatsResponse reservedSeatsResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getReservedSeatsCircuitBreaker",
                () -> eventServiceClient.getReservedSeats(userId, eventScheduleId));

        if (!reservedSeatsResponse.getReservedSeats().equals(seatMappingIds)) {
            throw new BusinessException(ErrorCode.NOT_MATCH_RESERVED_SEATS);
        }

        log.info("해당 유저가 선점한 좌석 : {}", reservedSeatsResponse.getReservedSeats());
    }

    private ValidSeatInfoResponse checkRequestValid(CreateOrderRequest createOrderRequest) {
        ValidSeatInfoResponse validSeatInfoResponse = executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "checkRequestValidCircuitBreaker",
                () -> eventServiceClient.checkRequest(createOrderRequest));

        if (createOrderRequest.getSelectSeatInfoList().equals(validSeatInfoResponse.getSelectSeatInfoList())) {
            throw new BusinessException(ErrorCode.INPUT_VALUE_INVALID);
        }

        return validSeatInfoResponse;

    }


    private boolean verifyPaidInfo(String paymentId) {
        Map<String, Object> paymentDetails = portOneApiClient.getPaymentDetails(paymentId);
        String status = (String) paymentDetails.get("status");
        Map<String, Integer> amount = (Map<String, Integer>) paymentDetails.get("amount");
        BigDecimal totalPrice = new BigDecimal(amount.get("total"));


        BigDecimal orderPrice = orderRepository.findOrderByPaymentId(paymentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER_PRICE));

        return ("PAID".equalsIgnoreCase(status) && orderPrice.compareTo(totalPrice) == 0);
    }

    private void notifyClient(String paymentId, String orderStatus) {
        Long userId = orderRepository.findUserIdByPaymentId(paymentId);

        executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "sendOrderStatusCircuitBreaker",
                () -> queueServiceClient.sendOrderStatus(userId, orderStatus)
        );
    }

    /**
     * 주문 환불을 처리하는 메서드.
     * 예매 당일 취소, 환불 정책 적용, 또는 부분 환불 여부를 판단하여 적절한 환불 처리를 수행합니다.
     *
     * @param orderId 환불하려는 주문의 ID
     * @throws BusinessException 주문이 없거나 환불 조건에 맞지 않을 경우 예외 발생
     */
    public void refundOrder(Long orderId) {
        // 주문 ID로 주문 정보 조회, 주문이 없거나 완료 상태가 아니면 예외 발생
        Orders order = orderRepository.findByIdCompletedStatus(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));

        if (!order.getTicket().getViewingStatus().equals(ViewingStatus.NOT_WATCHED)) {
            throw new BusinessException(ErrorCode.ALREADY_WATCHED);
        }

        // 관람일 정보 조회
        LocalDateTime eventDateTime = eventServiceClient.getEventDateTime(order.getTicket().getEventScheduleId());
        LocalDateTime orderDateTime = order.getCreatedAt(); // 주문 생성 시간
        LocalDateTime currentDateTime = LocalDateTime.now(); // 현재 시간

        // 1. 예매 당일 밤 12시 이전 취소 시 수수료 없음
        // 예매일(orderDateTime)과 현재일(currentDateTime)이 같으면 즉시 전체 환불 처리
        if (orderDateTime.toLocalDate().equals(currentDateTime.toLocalDate())) {
            processFullRefund(order, order.getOrderPrice());
            // settlement 수정
            executeWithCircuitBreaker(
                    circuitBreakerRegistry,
                    "refundSettlementCircuitBreaker",
                    () -> adminServiceClient.refundSettlement(orderId, BigDecimal.ZERO)
            );
            return; // 환불 처리 완료 후 메서드 종료
        }

        // 2. 환불 정책 조회 (우선순위 기준으로 정렬)
        // 환불 정책은 관람일 또는 예매일 기준으로 수수료 계산에 사용
        List<RefundPolicy> refundPolicies = refundPolicyRepository.findAllOrderByPriority();

        // 3. 관람일 기준과 예매일 기준 비교하여 수수료 설명 반환
        // 우선순위가 높은 환불 정책을 기준으로 수수료를 결정
        String refundFeeDescription = determineRefundFee(refundPolicies, currentDateTime, orderDateTime, eventDateTime);

        // 수수료 적용 여부에 따라 환불 처리
        if (refundFeeDescription.equals("없음")) {
            // 수수료가 없는 경우 - 전체 금액 환불 처리
            processFullRefund(order, order.getOrderPrice());
            // settlement 수정
            executeWithCircuitBreaker(
                    circuitBreakerRegistry,
                    "refundSettlementCircuitBreaker",
                    () -> adminServiceClient.refundSettlement(orderId, BigDecimal.ZERO)
            );

        } else {
            // 수수료가 적용되는 경우 - 부분 환불 처리
            BigDecimal refundAmount = calculateRefundAmount(order.getOrderPrice(), refundFeeDescription);
            processPartialRefund(order, refundAmount);
            // settlement 수정
            executeWithCircuitBreaker(
                    circuitBreakerRegistry,
                    "refundSettlementCircuitBreaker",
                    () -> adminServiceClient.refundSettlement(orderId, refundAmount)
            );
        }
    }

    /**
     * 전체 환불 처리
     *
     * @param order        주문 객체
     * @param refundAmount 환불 금액
     */
    private void processFullRefund(Orders order, BigDecimal refundAmount) {
        portOneApiClient.cancelOrder(order.getPaymentId());
        notifyRefundSuccess(order, refundAmount);
    }

    /**
     * 부분 환불 처리
     *
     * @param order        주문 객체
     * @param refundAmount 환불 금액
     */
    private void processPartialRefund(Orders order, BigDecimal refundAmount) {
        portOneApiClient.partialCancelOrder(order.getPaymentId(), refundAmount);
        notifyRefundSuccess(order, refundAmount);
    }

    /**
     * 환불 성공 알림 처리
     *
     * @param order        주문 객체
     * @param refundAmount 환불 금액
     */
    private void notifyRefundSuccess(Orders order, BigDecimal refundAmount) {
        ResponseEntity<Void> refundTicketCircuitBreaker = executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "refundTicketCircuitBreaker",
                () -> eventServiceClient.refundTicket(new TicketInfo(order.getTicket().getTicketId(), order.getTicket().getEventScheduleId()))
        );

        if (refundTicketCircuitBreaker.getStatusCode().is2xxSuccessful()) {
            order.refund(refundAmount);
        } else {
            throw new BusinessException(ErrorCode.REFUND_FAILED);
        }
    }

    /**
     * 환불 수수료 정책 결정
     *
     * @param refundPolicies  환불 정책 목록
     * @param currentDateTime 현재 시간
     * @param orderDateTime   주문 생성 시간
     * @param eventDateTime   관람일 시간
     * @return 적용할 환불 정책의 수수료 설명
     */
    private String determineRefundFee(List<RefundPolicy> refundPolicies, LocalDateTime currentDateTime, LocalDateTime orderDateTime, LocalDateTime eventDateTime) {
        for (RefundPolicy policy : refundPolicies) {
            if (matchesPolicy(currentDateTime, orderDateTime, eventDateTime, policy)) {
                return policy.getRefundFeeDescription();
            }
        }
        throw new BusinessException(ErrorCode.NO_MATCHING_REFUND_POLICY);
    }

    /**
     * 환불 정책 조건 확인
     *
     * @param currentDateTime 현재 시간
     * @param orderDateTime   주문 생성 시간
     * @param eventDateTime   관람일 시간
     * @param policy          환불 정책
     * @return 해당 환불 정책이 조건에 맞는지 여부
     */
    private boolean matchesPolicy(LocalDateTime currentDateTime, LocalDateTime orderDateTime, LocalDateTime eventDateTime, RefundPolicy policy) {
        // 관람일 기준 우선
        if (policy.getCancellationPeriod().contains("관람일")) {
            int daysBeforeEvent = extractDaysBefore(policy.getCancellationPeriod());
            if (daysBeforeEvent != -1 && currentDateTime.isBefore(eventDateTime.minusDays(daysBeforeEvent))) {
                return true;
            }
        }

        // 예매일 기준
        if (policy.getCancellationPeriod().contains("예매 후")) {
            int daysAfterBooking = extractDaysAfter(policy.getCancellationPeriod());
            if (daysAfterBooking != -1 && currentDateTime.isBefore(orderDateTime.plusDays(daysAfterBooking))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 정책 기간에서 "관람일 X일 전" 형태의 X값 추출
     *
     * @param cancellationPeriod 환불 정책의 취소 기간
     * @return 관람일 기준의 X일 전 값
     */
    private int extractDaysBefore(String cancellationPeriod) {
        if (cancellationPeriod.matches("관람일 (\\d+)일 전.*")) {
            return Integer.parseInt(cancellationPeriod.replaceAll("[^0-9]", ""));
        }
        return -1;
    }

    /**
     * 정책 기간에서 "예매 후 X일 이내" 형태의 X값 추출
     *
     * @param cancellationPeriod 환불 정책의 취소 기간
     * @return 예매일 기준의 X일 값
     */
    private int extractDaysAfter(String cancellationPeriod) {
        if (cancellationPeriod.matches("예매 후 (\\d+)일 이내.*")) {
            return Integer.parseInt(cancellationPeriod.replaceAll("[^0-9]", ""));
        }
        return -1;
    }

    /**
     * 수수료 설명에 따라 환불 금액 계산
     *
     * @param totalAmount          총 주문 금액
     * @param refundFeeDescription 환불 수수료 설명
     * @return 환불 금액
     */
    private BigDecimal calculateRefundAmount(BigDecimal totalAmount, String refundFeeDescription) {
        if (refundFeeDescription.contains("장당")) {
            // 고정 금액 수수료 (예: "장당 4,000원")
            String fee = refundFeeDescription.replaceAll("[^0-9]", "");
            return totalAmount.subtract(new BigDecimal(fee));
        } else if (refundFeeDescription.contains("%")) {
            // 퍼센트 수수료 (예: "티켓 금액의 20%")
            String percentage = refundFeeDescription.replaceAll("[^0-9]", "");
            BigDecimal feePercentage = new BigDecimal(percentage).divide(new BigDecimal(100));
            return totalAmount.subtract(totalAmount.multiply(feePercentage));
        }
        return totalAmount;
    }

    /**
     * 사용자가 구매한 티켓 정보를 조회하는 메서드.
     * 사용자 ID를 기반으로 티켓 ID 목록을 가져오고, 외부 서비스와 통신하여 상세 정보를 조회합니다.
     *
     * @param userId 사용자 ID
     * @return 사용자의 티켓 정보 목록 (TicketInfoDto 리스트)
     * @throws BusinessException 외부 서비스 호출 실패 또는 Circuit Breaker 트리거 시 예외 발생 가능
     */
    public List<TicketInfoDto> getMyTickets(Long userId) {
        // 사용자 ID를 기반으로 구매한 티켓 ID 목록 조회
        List<TicketInfoCreateDto> myTicketIds = orderRepository.findTicketIdsByUserId(userId);

        // Circuit Breaker를 사용하여 외부 서비스 호출
        // eventServiceClient를 통해 티켓 ID 목록을 기반으로 상세 티켓 정보를 가져옴
        return executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getMyTicketInfoCircuitBreaker", // Circuit Breaker 이름
                () -> eventServiceClient.getMyTicketInfo(new TicketInfoCreateDtoList(myTicketIds))
        );
    }

    /**
     * 티켓팅 엔트리 포인트로, 사용자가 특정 이벤트 스케줄에 대해 남은 구매 가능 티켓 수를 확인하고,
     * 구매 가능 수량이 0일 경우 예외를 발생시킵니다.
     *
     * @param userId          사용자의 고유 ID
     * @param eventScheduleId 이벤트 스케줄의 고유 ID
     * @return 구매 가능 티켓 수 (0 이상)
     * @throws BusinessException 구매 가능 티켓 수가 0일 경우 예외 발생
     */
    public Integer enterTicketing(Long userId, Long eventScheduleId) {
        // 주어진 사용자와 이벤트 스케줄에 대해 구매된 티켓 ID 목록 조회
        List<Long> ticketDtoList = orderRepository.findTicketIdByEventSchedule(userId, eventScheduleId);

        // Circuit Breaker를 사용하여 외부 서비스 호출
        Integer availableCount = executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getAvailableCountCircuitBreaker",
                () -> eventServiceClient.getAvailableCount(ticketMapper.toTicketDto(eventScheduleId, ticketDtoList))
        );

        // 남은 구매 가능 티켓 수가 0일 경우 예외를 발생
        if (availableCount <= 0) {
            throw new BusinessException(ErrorCode.PURCHASE_LIMIT_MET);
        }

        // 남은 구매 가능 티켓 수 반환
        return availableCount;
    }

    public int[] getTicketUserStatistic(List<Long> scheduleIdList) {
        List<Long> userIds = new ArrayList<>();
        for (Long id : scheduleIdList) {
            ticketRepository.findAllByEventScheduleId(id)
                    .forEach(ticket -> {
                        Orders order = orderRepository.findByTicket(ticket)
                                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));
                        userIds.add(order.getUserId());
                    });
        }
        List<UserSimpleDto> users = userServiceClient.getUsers(userIds);
        int male = 0;
        int female = 0;
        int age10 = 0;
        int age20 = 0;
        int age30 = 0;
        int age40 = 0;
        int age50 = 0;
        for (UserSimpleDto user : users) {
            if (user.getGender().equals(Gender.MALE)) {
                male++;
            } else {
                female++;
            }
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            int age = (currentYear - user.getBirth()) / 10;
            switch (age) {
                case 1:
                    age10++;
                    break;
                case 2:
                    age20++;
                    break;
                case 3:
                    age30++;
                    break;
                case 4:
                    age40++;
                    break;
                default:
                    age50++;
                    break;
            }
        }

        int[] res = {male, female, age10, age20, age30, age40, age50};
        return res;
    }

    /**
     * 날짜별 수익 데이터를 계산합니다.
     *
     * @param ticketIds 계산할 티켓 ID 목록
     * @return 날짜별 수익 데이터 리스트
     */
    public List<DailyRevenueResponse> calculateDailyRevenue(Set<Long> ticketIds) {
        return orderRepository.calculateDailyRevenue(ticketIds);
    }

    /**
     * 요일별 예매 수를 계산합니다.
     *
     * @param ticketIds 계산할 티켓 ID 목록
     * @return 요일별 예매 수 데이터 (DayCountResponse 객체)
     */
    public DayCountResponse calculateDayCount(Set<Long> ticketIds) {
        // 이번 주 월요일과 일요일 계산
        LocalDate today = LocalDate.now();
        LocalDate startOfWeek = today.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = today.with(DayOfWeek.SUNDAY);

        // 시작과 끝 날짜를 LocalDateTime으로 변환
        LocalDateTime startOfWeekTime = startOfWeek.atStartOfDay(); // 월요일 00:00:00
        LocalDateTime endOfWeekTime = endOfWeek.atTime(23, 59, 59); // 일요일 23:59:59

        // 쿼리 실행
        List<Object[]> results = orderRepository.calculateDayCount(ticketIds, startOfWeekTime, endOfWeekTime);

        // 요일별 초기화
        Map<String, Long> dayCountMap = new LinkedHashMap<>();
        List<String> daysOrder = List.of("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday");
        daysOrder.forEach(day -> dayCountMap.put(day, 0L)); // 초기값 0

        // 쿼리 결과를 맵에 추가
        for (Object[] result : results) {
            String day = (String) result[0];
            Long count = (Long) result[1];
            dayCountMap.put(day, count);
        }

        return new DayCountResponse(dayCountMap);
    }

    public List<OrderInfoDto> getCustomerTickets(Long userId) {

        List<OrderInfoDto> res = new ArrayList<>();
        List<Orders> customerOrders = orderRepository.findAllByUserId(userId);

        for (Orders order : customerOrders) {
            TicketSimpleInfo ticketInfo = executeWithCircuitBreaker(
                    circuitBreakerRegistry,
                    "getTicketSimpleInfo",
                    () -> eventServiceClient.getTicketSimpleInfo(order.getOrderId()));

            res.add(OrderInfoDto.builder()
                    .orderId(order.getOrderId())
                    .ticketTotalPrice(order.getOrderPrice())
                    .eventTitle(ticketInfo.getEventTitle())
                    .seatLoc(ticketInfo.getSeatLoc())
                    .stageName(ticketInfo.getStageName())
                    .createdAt(order.getCreatedAt())
                    .build());
        }
        return res;
    }

    public FaceApiResponse uploadUserFace(MultipartFile userFaceImage, Long EventScheduleId) {
        FaceApiResponse faceApiResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                "postUserFaceImgCircuitBreaker",
                () -> faceServiceClient.uploadFace(userFaceImage, EventScheduleId)
        );

        log.info(faceApiResponse.getMessage());

        return faceApiResponse;
    }

    public void matchFace(MultipartFile userFaceImage, Long eventId, Long connectId) {
        List<Long> eventScheduleIds = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getEventScheduleIdList",
                () -> eventServiceClient.getScheduledId(eventId));

        for (Long eventScheduleId : eventScheduleIds) {
            FaceApiResponse faceApiResponse = null;
            try {
                faceApiResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "postMatchUserFaceImgCircuitBreaker",
                        () -> faceServiceClient.matchFace(userFaceImage, eventScheduleId)
                );
            } catch (Exception e) {
                log.info("히히 : {}", eventScheduleId );
            }

            if (faceApiResponse != null && faceApiResponse.getStatus() == 200) {
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> map = objectMapper.convertValue(faceApiResponse.getData(), Map.class);
                Long ticketId = ((Number) map.get("ticket_id")).longValue();
                TicketSimpleInfo ticketSimpleInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getCustomerSeat",
                        () -> eventServiceClient.getTicketSimpleInfo(ticketId));

                Ticket ticket = ticketRepository.findById(ticketId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TICKET));
                Orders order = orderRepository.findByTicket(ticket)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));

                UserSimpleDto userInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getUserByIdCircuitBreaker",
                        () -> userServiceClient.getUser(order.getUserId())
                );
                CheckDto message = CheckDto.builder()
                        .data(faceApiResponse.getData())
                        .name(userInfo.getUserName())
                        .birth(userInfo.getBirth())
                        .seatLoc(ticketSimpleInfo.getSeatLoc())
                        .build();
                checkService.sendMessage(eventId, connectId, message);
                break;
            }

        }
    }

    /**
     * 주문 생성 실패 시 보상적 트랜잭션 실행
     *
     * @param orderId 취소할 orderId
     */
    public void cancelOrder(Long orderId) {
        Orders findOrder = orderRepository.findByOrderId(orderId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));
        portOneApiClient.cancelOrder(findOrder.getPaymentId());
    }
}