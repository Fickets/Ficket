package com.example.ficketticketing.domain.order.service;

import com.example.ficketticketing.domain.order.client.*;
import com.example.ficketticketing.domain.order.dto.client.*;
import com.example.ficketticketing.domain.order.dto.kafka.OrderDto;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.request.SelectSeatInfo;
import com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDto;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDtoList;
import com.example.ficketticketing.domain.order.entity.Orders;
import com.example.ficketticketing.domain.order.messagequeue.OrderProducer;
import com.example.ficketticketing.domain.order.repository.OrderRepository;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.example.ficketticketing.infrastructure.payment.PortOneApiClient;
import com.example.ficketticketing.infrastructure.payment.dto.WebhookPayload;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
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
    private final PaymentSseService paymentSseService;
    private final OrderRepository orderRepository;
    private final UserServiceClient userServiceClient;
    private final EventServiceClient eventServiceClient;
    private final FaceServiceClient faceServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final OrderProducer orderProducer;

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

//        // 중복 이벤트 방지: 이미 완료되었거나 취소된 결제는 무시
//        if (orderRepository.existsByPaymentIdAndOrderStatus(paymentId, OrderStatus.COMPLETED)) {
//            log.info("이미 처리된 결제: {}", paymentId);
//            return; // 중복 이벤트 무시
//        }
//
//        if (orderRepository.existsByPaymentIdAndOrderStatus(paymentId, OrderStatus.CANCELLED)) {
//            log.info("이미 취소된 결제: {}", paymentId);
//            return; // 중복 이벤트 무시
//        }

        log.info("Processing event type: {}", type);
        switch (type) {
            case "Transaction.Ready":
                log.info("결제창 오픈");
                break;
            case "Transaction.Paid":
                if (verifyPaidInfo(paymentId)) {
                    orderRepository.updateOrderStatusToCompleted(paymentId);
                    notifyClient(paymentId, "PAID");
                } else {
                    portOneApiClient.cancelOrder(paymentId);
                    notifyClient(paymentId, "FAILED");
                }
                break;
            case "Transaction.Cancelled":
                log.info("결제 취소 처리");
                // TODO: 결제 취소 처리 로직 구현
                break;
            default:
                log.warn("알 수 없는 이벤트 타입: {}", type);
                throw new BusinessException(ErrorCode.INVALID_EVENT_TYPE);
        }
    }

    public Long createOrder(CreateOrderRequest createOrderRequest, MultipartFile userFaceImage, Long userId) {
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

        FaceApiResponse faceApiResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                "postUserFaceImgCircuitBreaker",
                () -> faceServiceClient.uploadFace(userFaceImage, createdOrder.getTicket().getTicketId(), createOrderRequest.getEventScheduleId())
        );


        if (faceApiResponse.getStatus() != 200) {
            log.info("사진 업로드 실패");
            throw new BusinessException(ErrorCode.FAILED_UPLOAD_USER_FACE);
        }

        log.info(faceApiResponse.getMessage());

        orderProducer.send("order-events", new OrderDto(createdOrder.getOrderId(), seatMappingIds, createdOrder.getTicket().getTicketId()));

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

    private void notifyClient(String paymentId, String status) {
        // SSE를 통해 클라이언트에 결제 상태 알림
        paymentSseService.notifyPaymentStatus(paymentId, status);
    }

    public void refundOrder(Long orderId) {
        Orders order = orderRepository.findById(orderId).orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_ORDER));
        portOneApiClient.cancelOrder(order.getPaymentId());
    }

    public List<TicketInfoDto> getMyTickets(Long userId) {
        // 사용자 티켓 ID 가져오기
        List<TicketInfoCreateDto> myTicketIds = orderRepository.findTicketIdsByUserId(userId);


        return executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getMyTicketInfoCircuitBreaker",
                () -> eventServiceClient.getMyTicketInfo(new TicketInfoCreateDtoList(myTicketIds))
        );
    }

}
