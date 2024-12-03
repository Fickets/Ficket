package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.dto.response.QEventSearchRes;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.ficketevent.domain.event.entity.QEvent.*;
import static com.example.ficketevent.domain.event.entity.QEventSchedule.*;

@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<EventSearchRes> searchEventByCond(EventSearchCond cond) {
        return queryFactory.selectDistinct(new QEventSearchRes(
                        event.eventId,
                        event.title,
                        event.eventStage.stageName,
                        event.companyId,
                        event.adminId,
                        eventSchedule.eventDate))
                .from(event)
                .join(event.eventSchedules, eventSchedule)
                .where(
                        eqEventId(cond.getEventId()),
                        containsEventTitle(cond.getEventTitle()),
                        eqCompanyId(cond.getCompanyId()),
                        eqAdminId(cond.getAdminId()),
                        eqEventStageId(cond.getEventStageId()),
                        goeStartDate(cond.getStartDate()),
                        loeEndDate(cond.getEndDate())
                )
                .fetch();
    }

    private BooleanExpression eqEventId(Long eventId) {
        return eventId != null ? event.eventId.eq(eventId) : null;
    }

    private BooleanExpression containsEventTitle(String eventTitle) {
        return eventTitle != null ? event.title.containsIgnoreCase(eventTitle) : null;
    }

    private BooleanExpression eqCompanyId(Long companyId) {
        return companyId != null ? event.companyId.eq(companyId) : null;
    }

    private BooleanExpression eqAdminId(Long adminId) {
        return adminId != null ? event.adminId.eq(adminId) : null;
    }

    private BooleanExpression eqEventStageId(Long eventStageId) {
        return eventStageId != null ? event.eventStage.stageId.eq(eventStageId) : null;
    }

    private BooleanExpression goeStartDate(LocalDateTime startDate) {
        return startDate != null ? eventSchedule.eventDate.goe(startDate) : null;
    }

    private BooleanExpression loeEndDate(LocalDateTime endDate) {
        return endDate != null ? eventSchedule.eventDate.loe(endDate) : null;
    }

}
