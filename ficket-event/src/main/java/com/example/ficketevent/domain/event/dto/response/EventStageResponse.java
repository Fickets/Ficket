package com.example.ficketevent.domain.event.dto.response;

import lombok.Data;

@Data
public class EventStageResponse {

    private Long stageId; // 공연장 ID

    private String stageName; // 공연장 이름

}
