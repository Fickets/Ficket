package com.example.ficketticketing.infrastructure.payment;

import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.example.ficketticketing.infrastructure.payment.dto.CancellationResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PortOneApiClient {

    private final WebClient webClient;

    @Value("${portone.api.base-url}")
    private String baseUrl;

    @Value("${portone.api.token}")
    private String apiToken;

    public Map<String, Object> getPaymentDetails(String paymentId) {
        try {
            return webClient
                    .get()
                    .uri(baseUrl + "/{paymentId}", paymentId)
                    .header("Authorization", "PortOne " + apiToken)
                    .retrieve()
                    .bodyToMono(Map.class)
                    .block(); // 블로킹 방식으로 결과 반환
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    public void cancelOrder(String paymentId) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "고객 요청");

        try {
            CancellationResponse result = webClient
                    .post()
                    .uri(baseUrl + "/{paymentId}/cancel", paymentId)
                    .header("Authorization", "PortOne " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(CancellationResponse.class)
                    .block();

            if (result == null || result.getCancellation() == null) {
                log.error("결제 취소 응답이 비어 있습니다: paymentId={}", paymentId);
                throw new BusinessException(ErrorCode.CANCEL_FAIL);
            }

            String responseStatus = result.getCancellation().getStatus();

            if (!"SUCCEEDED".equals(responseStatus)) {
                log.warn("결제 취소 실패: paymentId={}, status={}", paymentId, responseStatus);
                throw new BusinessException(ErrorCode.CANCEL_FAIL);
            }

            log.info("{} 결제 취소 성공", paymentId);

        } catch (WebClientResponseException e) {
            log.error("결제 취소 요청 실패: paymentId={}, statusCode={}, responseBody={}",
                    paymentId, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.CANCEL_FAIL);
        } catch (Exception ex) {
            log.error("결제 취소 처리 중 오류 발생: paymentId={}", paymentId, ex);
            throw new BusinessException(ErrorCode.CANCEL_FAIL);
        }
    }


    public void partialCancelOrder(String paymentId, BigDecimal refundAmount) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("reason", "고객 요청");
        requestBody.put("amount", refundAmount); // amount 값을 설정

        try {
            CancellationResponse result = webClient
                    .post()
                    .uri(baseUrl + "/{paymentId}/cancel", paymentId)
                    .header("Authorization", "PortOne " + apiToken)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(requestBody)
                    .retrieve()
                    .bodyToMono(CancellationResponse.class)
                    .block();

            if (result == null || result.getCancellation() == null) {
                log.error("부분 결제 취소 응답이 비어 있습니다: paymentId={}, refundAmount={}", paymentId, refundAmount);
                throw new BusinessException(ErrorCode.CANCEL_FAIL);
            }

            String responseStatus = result.getCancellation().getStatus();

            if ("SUCCEEDED".equals(responseStatus)) {
                log.info("부분 결제 취소 성공: paymentId={}, refundAmount={}", paymentId, refundAmount);
                return;
            }

            log.warn("부분 결제 취소 실패: paymentId={}, status={}, refundAmount={}", paymentId, responseStatus, refundAmount);
            throw new BusinessException(ErrorCode.CANCEL_FAIL);

        } catch (WebClientResponseException e) {
            log.error("부분 결제 취소 요청 실패: paymentId={}, refundAmount={}, statusCode={}, responseBody={}",
                    paymentId, refundAmount, e.getStatusCode(), e.getResponseBodyAsString(), e);
            throw new BusinessException(ErrorCode.CANCEL_FAIL);
        } catch (Exception ex) {
            log.error("부분 결제 취소 처리 중 오류 발생: paymentId={}, refundAmount={}", paymentId, refundAmount, ex);
            throw new BusinessException(ErrorCode.CANCEL_FAIL);
        }
    }

}