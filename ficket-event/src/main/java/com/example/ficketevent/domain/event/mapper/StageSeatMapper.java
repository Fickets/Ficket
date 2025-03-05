package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.response.SeatResponse;
import com.example.ficketevent.domain.event.entity.StageSeat;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface StageSeatMapper {

    SeatResponse toStageSeatDto(StageSeat stageSeat);
}
