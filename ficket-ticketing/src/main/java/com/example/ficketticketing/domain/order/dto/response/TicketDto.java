package com.example.ficketticketing.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TicketDto {

    private Long eventScheduleId;
    private List<Long> ticketIds;

}
