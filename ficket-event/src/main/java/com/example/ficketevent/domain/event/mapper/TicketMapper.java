package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.common.TicketInfoDto;
import com.example.ficketevent.domain.event.dto.response.TicketEventResponse;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.Map;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    @Mapping(source = "companyId", target = "companyName", qualifiedByName = "mapCompanyName")
    TicketInfoDto toTicketInfoDto(TicketEventResponse ticketEventResponse, @Context Map<Long, String> companyNameMap);

    @Named("mapCompanyName")
    default String mapCompanyName(Long companyId, @Context Map<Long, String> companyNameMap) {
        return companyNameMap.getOrDefault(companyId, "Unknown");
    }
}