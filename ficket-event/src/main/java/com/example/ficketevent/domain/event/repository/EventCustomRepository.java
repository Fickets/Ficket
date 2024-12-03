package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;

import java.util.List;

public interface EventCustomRepository {
    List<EventSearchRes> searchEventByCond(EventSearchCond eventSearchCond);
}
