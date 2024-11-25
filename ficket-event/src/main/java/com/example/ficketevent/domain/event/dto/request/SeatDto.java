package com.example.ficketevent.domain.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SeatDto {
    private String grade; // 좌석 등급
    private BigDecimal price; // 가격
    private List<Long> seats; // 좌석 번호 목록
}