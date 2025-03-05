package com.example.ficketevent.domain.event.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class EventStageListResponse {
    private List<EventStageResponse> eventStageDtoList;

    public EventStageListResponse(List<EventStageResponse> eventStageDtoList) {
        this.eventStageDtoList = eventStageDtoList;
    }
}
