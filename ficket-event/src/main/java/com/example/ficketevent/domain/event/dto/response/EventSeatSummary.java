package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class EventSeatSummary {
    private String getPosterMobileUrl;
    private Integer reservationLimit;
    private String eventStageImg;
    private List<SeatGradeInfo> seatGradeInfoList;

}
