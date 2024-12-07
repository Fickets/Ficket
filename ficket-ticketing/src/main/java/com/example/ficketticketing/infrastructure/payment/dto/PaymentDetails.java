package com.example.ficketticketing.infrastructure.payment.dto;

import lombok.Data;

@Data
public class PaymentDetails {
    private String status;
    private String id;
    private String transactionId;
    private String merchanId;
    private String storeId;
}
