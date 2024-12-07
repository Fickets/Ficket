package com.example.ficketticketing.domain.order.dto.request;

import lombok.Data;

import java.util.List;


@Data
public class CreateOrderRequest {
    private String paymentId;
    private Long eventScheduleId;
    private Integer reservationLimit;
    private List<SelectSeatInfo> selectSeatInfoList;

}
