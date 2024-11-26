package com.example.ficketevent.domain.event.dto.request;

import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
public class EventDateDto {
    private LocalDate date; // 날짜
    private List<SessionDto> sessions; // 세션 정보

}
