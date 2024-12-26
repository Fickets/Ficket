package com.example.ficketticketing.domain.order.dto.client;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderSimpleDto {
    private Long orderId;
    private BigDecimal orderPrice;
    private BigDecimal refundPrice;
    private Long companyId;
    private Long eventId;
}
