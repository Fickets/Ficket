package com.example.ficketevent.domain.event.dto.common;

import com.example.ficketevent.domain.event.dto.request.SelectSeatInfo;
import lombok.Data;

import java.util.List;


@Data
public class CreateOrderRequest {
    private String paymentId;
    private Long eventScheduleId;
    private List<SelectSeatInfo> selectSeatInfoList;

}
