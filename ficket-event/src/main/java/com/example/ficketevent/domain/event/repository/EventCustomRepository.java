package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventScheduledOpenSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventScheduledOpenResponse;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.dto.response.SimpleEvent;
import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.enums.Genre;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface EventCustomRepository {
//    List<EventSearchRes> searchEventByCond(EventSearchCond eventSearchCond, Pageable pageable);

    List<Long> getEventScheduleByEventId(Long eventId);

    Set<Long> getTicketIdsByEventId(@Param("eventId") Long eventId);

    Page<EventScheduledOpenResponse> searchEventScheduledOpen(EventScheduledOpenSearchCond cond, Pageable pageable);

    List<SimpleEvent> openSearchTop6Genre(String genre);

    List<SimpleEvent> getGenreRank(String genre);

    Page<Event> findExcludingIds(List<Long> ids, Genre genre, String area, Pageable pageable);

    List<Event> findIdsArea(List<Long> ids, String area);
}
