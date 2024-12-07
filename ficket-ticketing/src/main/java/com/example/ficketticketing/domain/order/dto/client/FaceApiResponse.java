package com.example.ficketticketing.domain.order.dto.client;

import lombok.Data;

@Data
public class FaceApiResponse {
    private int status;
    private String message;
    private Object data; // 데이터의 구조에 따라 타입을 변경
}