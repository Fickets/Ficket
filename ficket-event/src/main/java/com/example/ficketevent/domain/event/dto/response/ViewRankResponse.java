package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.dto.common.EventScheduleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ViewRankResponse {

    private Long eventId;
    private String eventTitle;
    private String eventSubTitle;
    private String eventStageName;
    private String eventOriginBannerUrl;
    private String eventPcPosterUrl;
    private Set<LocalDate> eventDateList;

    public static ViewRankResponse toViewRankResponse(Long eventId, EventDetailRes eventDetailRes) {

        Set<LocalDate> eventDateParsingList = new TreeSet<>();

        for (HashMap<Integer, EventScheduleDto> value : eventDetailRes.getScheduleMap().values()) {
            for (EventScheduleDto eventScheduleDto : value.values()) {
                eventDateParsingList.add(eventScheduleDto.getEventDate().toLocalDate());
            }
        }

        return ViewRankResponse.builder()
                .eventId(eventId)
                .eventTitle(eventDetailRes.getTitle())
                .eventSubTitle(eventDetailRes.getSubTitle())
                .eventStageName(eventDetailRes.getStageName())
                .eventDateList(eventDateParsingList)
                .eventOriginBannerUrl(eventDetailRes.getBannerOriginUrl())
                .eventPcPosterUrl(eventDetailRes.getPosterPcMainUrl())
                .build();
    }
}
