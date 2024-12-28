package com.example.ficketevent.domain.event.dto.response;

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

    private String pcImg;
    private String mobileImg;
}
