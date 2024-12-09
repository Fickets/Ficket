package com.example.ficketticketing.infrastructure.payment;

import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

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

        Map cancelResultMap = webClient
                .delete()
                .uri(baseUrl + "/{paymentId}", paymentId)
                .header("Authorization", "PortOne " + apiToken)
                .retrieve()
                .bodyToMono(Map.class)
                .block();

        if (cancelResultMap.get("Status").equals("SUCCEEDED")) {
            log.info("{} 결제 취소 성공", paymentId);
            return;
        }

        throw new BusinessException(ErrorCode.CANCEL_FAIL);

    }
}