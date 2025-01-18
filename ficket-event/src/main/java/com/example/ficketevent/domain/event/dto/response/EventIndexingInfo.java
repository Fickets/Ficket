package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.enums.Genre;
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
public class EventIndexingInfo {

    private Long eventId;
    private String title;
    private String stageName;
    private String sido;
    private String posterUrl;
    private LocalDateTime ticketingTime;
    private List<String> genreList;
    private List<LocalDateTime> eventDateList;


}
