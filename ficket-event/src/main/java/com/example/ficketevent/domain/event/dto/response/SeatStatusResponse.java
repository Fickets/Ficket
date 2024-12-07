package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatStatusResponse {
    private Long seatMappingId;
    private Double seatX;
    private Double seatY;
    private String seatGrade;
    private String seatRow;
    private String seatCol;
    private String status; // "AVAILABLE", "PURCHASED", "LOCKED"
    private BigDecimal seatPrice;
}