package com.example.ficketticketing.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class WebhookPayload {

    private String type;        // 이벤트 타입 (e.g., Transaction.Paid)
    private String timestamp;   // 타임스탬프

    @JsonProperty("data")
    private WebhookData data;   // 데이터 객체

    @Data
    public static class WebhookData {
        private String transactionId;  // 거래 ID
        private String paymentId;      // 결제 ID
        private String cancellationId;
    }
}