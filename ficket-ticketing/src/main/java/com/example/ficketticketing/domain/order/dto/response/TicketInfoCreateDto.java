package com.example.ficketticketing.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoCreateDto {
    private Long orderId;
    private Long ticketId;
    private LocalDateTime createdAt;

}
