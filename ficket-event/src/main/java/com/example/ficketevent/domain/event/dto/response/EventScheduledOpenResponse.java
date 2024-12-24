package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.enums.Genre;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
public class EventScheduledOpenResponse {

    private Long eventId;
    private String title;
    private List<Genre> genreList;
    private LocalDateTime ticketStartTime;
    private String mobilePosterUrl;
    private boolean isNewPostEvent;


    @QueryProjection
    public EventScheduledOpenResponse(Long eventId, String title, List<Genre> genreList, LocalDateTime ticketStartTime, String mobilePosterUrl, boolean isNewPostEvent) {
        this.eventId = eventId;
        this.title = title;
        this.genreList = genreList;
        this.ticketStartTime = ticketStartTime;
        this.mobilePosterUrl = mobilePosterUrl;
        this.isNewPostEvent = isNewPostEvent;
    }
}
