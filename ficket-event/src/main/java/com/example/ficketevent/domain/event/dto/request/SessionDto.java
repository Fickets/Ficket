package com.example.ficketevent.domain.event.dto.request;

import lombok.Getter;

@Getter
public class SessionDto {
    private Integer round; // 회차
    private String time; // 시작 시간 (HH:mm 포맷)

}