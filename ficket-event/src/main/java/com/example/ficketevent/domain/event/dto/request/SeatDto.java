package com.example.ficketevent.domain.event.dto.request;

import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
public class SeatDto {
    private String grade; // 좌석 등급
    private BigDecimal price; // 가격
    private List<Long> seats; // 좌석 번호 목록
}