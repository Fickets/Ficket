package com.example.ficketevent.domain.event.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PartitionDto {

    private String partitionName;
    @Builder.Default
    private int remainingSeats = 0;
}
