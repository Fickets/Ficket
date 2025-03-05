package com.example.ficketevent.domain.event.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SelectSeat {

    private Long eventScheduleId;
    private List<SelectSeatInfo> selectSeatInfoList;
}
