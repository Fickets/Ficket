package com.example.ficketticketing.infrastructure.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CancellationResponse {

    @JsonProperty("cancellation")
    private PaymentCancellation cancellation;

}
