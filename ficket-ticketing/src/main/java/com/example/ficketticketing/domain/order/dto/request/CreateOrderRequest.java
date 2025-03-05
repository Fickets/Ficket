package com.example.ficketticketing.domain.order.dto.request;

import lombok.Data;

import java.util.Set;


@Data
public class CreateOrderRequest {
    private String paymentId;
    private Long eventScheduleId;
    private Long faceId;
    private String faceImgUrl;
    private Set<SelectSeatInfo> selectSeatInfoList;
}
