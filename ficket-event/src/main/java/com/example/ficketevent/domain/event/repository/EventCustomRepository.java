package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface EventCustomRepository {
    List<EventSearchRes> searchEventByCond(EventSearchCond eventSearchCond);

    List<Long> getEventScheduleByEventId(Long eventId);

    Set<Long> getTicketIdsByEventId(@Param("eventId") Long eventId);
}
