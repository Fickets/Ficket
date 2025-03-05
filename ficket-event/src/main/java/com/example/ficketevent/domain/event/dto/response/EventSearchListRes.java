package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventSearchListRes {
    private Long eventId;
    private String eventTitle;
    private String stageName;
    private Long adminId;
    private String companyName;
    private String adminName;
    private List<LocalDateTime> eventDates;

}
