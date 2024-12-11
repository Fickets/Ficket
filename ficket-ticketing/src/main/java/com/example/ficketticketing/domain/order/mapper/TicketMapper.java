package com.example.ficketticketing.domain.order.mapper;

import com.example.ficketticketing.domain.order.dto.response.TicketDto;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketDto toTicketDto(Long eventScheduleId, List<Long> ticketIds);
}
