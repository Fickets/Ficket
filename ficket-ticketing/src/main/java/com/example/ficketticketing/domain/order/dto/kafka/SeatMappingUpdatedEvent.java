package com.example.ficketticketing.domain.order.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatMappingUpdatedEvent {
    private Long orderId;  // 주문 ID
    private boolean success;  // 업데이트 성공 여부
}