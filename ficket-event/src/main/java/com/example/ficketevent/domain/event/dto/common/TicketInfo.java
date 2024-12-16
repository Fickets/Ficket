package com.example.ficketevent.domain.event.dto.common;

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
