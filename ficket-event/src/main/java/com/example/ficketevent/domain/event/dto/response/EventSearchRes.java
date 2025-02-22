package com.example.ficketevent.domain.event.dto.response;

import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
public class EventSearchRes {
    private Long eventId;
    private String eventTitle;
    private String stageName;
    private Long companyId;
    private Long adminId;
    private LocalDateTime eventDate;

    @QueryProjection
    public EventSearchRes(Long eventId, String eventTitle, String stageName, Long companyId, Long adminId, LocalDateTime eventDate) {
        this.eventId = eventId;
        this.eventTitle = eventTitle;
        this.stageName = stageName;
        this.companyId = companyId;
        this.adminId = adminId;
        this.eventDate = eventDate;
    }
}
