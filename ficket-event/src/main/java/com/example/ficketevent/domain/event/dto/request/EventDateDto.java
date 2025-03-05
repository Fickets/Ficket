package com.example.ficketevent.domain.event.dto.request;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDateDto {
    private LocalDate date; // 날짜
    private List<SessionDto> sessions; // 세션 정보

    // LocalDate와 LocalTime을 결합하여 LocalDateTime 리스트로 변환
    public List<LocalDateTime> toLocalDateTimes() {
        return sessions.stream()
                .map(session -> LocalDateTime.of(date, LocalTime.parse(session.getTime())))
                .toList();
    }
}
