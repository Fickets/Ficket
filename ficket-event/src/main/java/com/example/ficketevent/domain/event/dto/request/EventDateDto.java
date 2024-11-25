package com.example.ficketevent.domain.event.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDateDto {
    private LocalDate date; // 날짜
    private List<SessionDto> sessions; // 세션 정보

}
