package com.example.ficketticketing.domain.order.dto.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfo {
    private Long ticketId;
    private Long eventScheduleId;
}
