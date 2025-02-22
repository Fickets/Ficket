package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventSeatSummary {
    private String posterMobileUrl;
    private Integer reservationLimit;
    private String eventStageImg;
    private List<SeatGradeInfo> seatGradeInfoList;

}
