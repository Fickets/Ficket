package com.example.ficketevent.domain.event.dto.kafka;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderDto {
    private Long eventScheduleId;
    private Long orderId;
    private Set<Long> seatMappingIds;
    private Long ticketId;
}
