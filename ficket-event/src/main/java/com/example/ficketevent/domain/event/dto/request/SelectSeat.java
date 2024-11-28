package com.example.ficketevent.domain.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectSeat {

    private Long eventScheduleId;
    private Integer reservationLimit;
    private Set<Long> seatMappingIds;
}
