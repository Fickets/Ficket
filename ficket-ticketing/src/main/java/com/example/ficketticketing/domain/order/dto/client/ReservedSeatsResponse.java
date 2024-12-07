package com.example.ficketticketing.domain.order.dto.client;

import lombok.Data;

import java.util.Set;

@Data
public class ReservedSeatsResponse {
    private Set<Long> reservedSeats; // 좌석 ID 목록
}