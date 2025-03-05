package com.example.ficketevent.domain.event.dto.common;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

@Data
@Builder
public class EventScheduleDto {
    private Long eventScheduleId;
    private Integer round;
    private LocalDateTime eventDate;

    @Builder.Default
    private HashMap<String, PartitionDto> partition = new HashMap<String, PartitionDto>();
}
