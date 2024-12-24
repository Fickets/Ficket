package com.example.ficketevent.domain.event.dto.request;

import com.example.ficketevent.domain.event.enums.Genre;
import lombok.Data;

@Data
public class EventScheduledOpenSearchCond {
    private String searchValue;
    private Genre genre;
}
