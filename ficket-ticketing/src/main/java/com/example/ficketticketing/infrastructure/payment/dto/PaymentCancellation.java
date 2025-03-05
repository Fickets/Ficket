package com.example.ficketticketing.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PaymentCancellation {

    @JsonProperty("status")
    private String status;

    @JsonProperty("id")
    private String id;

    @JsonProperty("pgCancellationId")
    private String pgCancellationId;

    @JsonProperty("totalAmount")
    private Integer totalAmount;

    @JsonProperty("taxFreeAmount")
    private Integer taxFreeAmount;

    @JsonProperty("vatAmount")
    private Integer vatAmount;

    @JsonProperty("easyPayDiscountAmount")
    private Integer easyPayDiscountAmount;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("cancelledAt")
    private String cancelledAt;

    @JsonProperty("requestedAt")
    private String requestedAt;

    @JsonProperty("trigger")
    private String trigger; // 추가 필드
}
