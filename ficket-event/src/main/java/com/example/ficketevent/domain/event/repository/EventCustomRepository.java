package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventScheduledOpenSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventScheduledOpenResponse;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.dto.response.SimpleEvent;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventCustomRepository {
    List<EventSearchRes> searchEventByCond(EventSearchCond eventSearchCond);

    List<Long> getEventScheduleByEventId(Long eventId);

    Set<Long> getTicketIdsByEventId(@Param("eventId") Long eventId);

    Page<EventScheduledOpenResponse> searchEventScheduledOpen(EventScheduledOpenSearchCond cond, Pageable pageable);

    List<SimpleEvent> openSearchTop6Genre(String genre);

    List<SimpleEvent> getGenreRank(String genre);
}
