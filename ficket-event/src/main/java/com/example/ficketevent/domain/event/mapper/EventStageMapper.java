package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.response.EventStageResponse;
import com.example.ficketevent.domain.event.entity.EventStage;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EventStageMapper {

    EventStageResponse toEventStageDto(EventStage eventStage);
}
