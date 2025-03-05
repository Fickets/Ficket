package com.example.ficketticketing.domain.order.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoCreateDtoList {
    List<TicketInfoCreateDto> ticketInfoCreateDtoList;
}
