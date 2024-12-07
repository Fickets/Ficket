package com.example.ficketevent.domain.event.dto.request;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class SelectSeatInfo {

    private Long seatMappingId;
    private BigDecimal seatPrice;
    private String seatGrade;
}
