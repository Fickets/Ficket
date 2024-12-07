package com.example.ficketticketing.domain.order.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SelectSeatInfo {

    private Long seatMappingId;
    private BigDecimal seatPrice;
    private String seatGrade;
}
