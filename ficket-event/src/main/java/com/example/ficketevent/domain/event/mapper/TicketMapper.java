package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.common.TicketInfoDto;
import com.example.ficketevent.domain.event.dto.response.TicketEventResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDateTime;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "companyId", target = "companyName", qualifiedByName = "mapCompanyName")
    @Mapping(source = "ticketId", target = "createdAt", qualifiedByName = "mapCreatedAt")
    @Mapping(source = "ticketId", target = "orderId", qualifiedByName = "mapOrderId")
    TicketInfoDto toTicketInfoDto(
            TicketEventResponse ticketEventResponse,
            @Context Map<Long, String> companyNameMap,
            @Context Map<Long, LocalDateTime> createdAtMap,
            @Context Map<Long, Long> ticketOrderMap
    );

    @Named("mapCompanyName")
    default String mapCompanyName(Long companyId, @Context Map<Long, String> companyNameMap) {
        return companyNameMap.getOrDefault(companyId, "Unknown");
    }

    @Named("mapCreatedAt")
    default LocalDateTime mapCreatedAt(Long ticketId, @Context Map<Long, LocalDateTime> createdAtMap) {
        return createdAtMap.getOrDefault(ticketId, LocalDateTime.now());
    }

    @Named("mapOrderId")
    default Long mapOrderId(Long ticketId, @Context Map<Long, Long> ticketOrderMap) {
        return ticketOrderMap.getOrDefault(ticketId, 0L);
    }
}