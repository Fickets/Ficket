package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.entity.Event;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class SimpleEvent {

    private Long eventId;
    private String title;

    private String date;

    private String eventStage;

    private String pcImg;
    private String mobileImg;
    private String mobileSmallImg;

    public static SimpleEvent from(Event event) {
        return SimpleEvent.builder()
                .eventId(event.getEventId())
                .title(event.getTitle())
                .date(event.getTicketingTime().toString())
                .pcImg(event.getEventImage().getPosterPcMain2Url())
                .mobileImg(event.getEventImage().getPosterPcMain1Url())
                .mobileSmallImg(event.getEventImage().getPosterMobileUrl())
                .build();
    }
}
