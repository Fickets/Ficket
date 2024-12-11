package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.entity.EventSchedule;

import java.util.List;
import java.util.Map;

public interface EventCustomRepository {
    List<EventSearchRes> searchEventByCond(EventSearchCond eventSearchCond);

    List<Long> getEventScheduleByEventId(Long eventId);
}
