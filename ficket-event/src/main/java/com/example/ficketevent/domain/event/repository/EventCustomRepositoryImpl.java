package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventScheduledOpenSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventScheduledOpenResponse;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
import com.example.ficketevent.domain.event.dto.response.QEventSearchRes;
import com.example.ficketevent.domain.event.dto.response.SimpleEvent;
import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.enums.Genre;
import com.example.ficketevent.domain.event.entity.QGenre;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.ficketevent.domain.event.entity.QEvent.*;
import static com.example.ficketevent.domain.event.entity.QEventSchedule.*;
import static com.example.ficketevent.domain.event.entity.QEventStage.eventStage;
import static com.example.ficketevent.domain.event.entity.QSeatMapping.seatMapping;
import static com.querydsl.jpa.JPAExpressions.selectFrom;

@Slf4j
@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository {

    private final JPAQueryFactory queryFactory;

//    @Override
//    public List<EventSearchRes> searchEventByCond(EventSearchCond cond, Pageable pageable) {
//        return queryFactory.selectDistinct(new QEventSearchRes(
//                        event.eventId,
//                        event.title,
//                        event.eventStage.stageName,
//                        event.companyId,
//                        event.adminId,
//                        eventSchedule.eventDate.min()
//                ))
//                .from(event)
//                .join(event.eventSchedules, eventSchedule)
//                .where(
//                        eqEventId(cond.getEventId()),
//                        containsEventTitle(cond.getEventTitle()),
//                        eqCompanyId(cond.getCompanyId()),
//                        eqAdminId(cond.getAdminId()),
//                        eqEventStageId(cond.getEventStageId()),
//                        goeStartDate(cond.getStartDate()),
//                        loeEndDate(cond.getEndDate())
//                )
//                .groupBy(
//                        event.eventId,
//                        event.title,
//                        event.eventStage.stageName,
//                        event.companyId,
//                        event.adminId,
//                        Expressions.dateTemplate(LocalDate.class, "DATE({0})", eventSchedule.eventDate)
//                )
//                .fetch();
//    }

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

    private BooleanExpression goeStartDate(LocalDate startDate) {
        return startDate != null
                ? Expressions.dateTemplate(LocalDate.class, "DATE({0})", eventSchedule.eventDate).goe(startDate)
                : null;
    }

    private BooleanExpression loeEndDate(LocalDate endDate) {
        return endDate != null
                ? Expressions.dateTemplate(LocalDate.class, "DATE({0})", eventSchedule.eventDate).loe(endDate)
                : null;
    }

    @Override
    public List<Long> getEventScheduleByEventId(Long eventId) {
        return queryFactory
                .select(eventSchedule.eventScheduleId) // EventSchedule의 id만 선택
                .from(event) // Event 테이블에서 시작
                .join(event.eventSchedules, eventSchedule) // Event -> EventSchedule 조인
                .where(event.eventId.eq(eventId)) // 조건: EventId와 일치
                .fetch(); // 결과를 리스트로 반환
    }

    @Override
    public Set<Long> getTicketIdsByEventId(Long eventId) {
        return new HashSet<>(
                queryFactory
                        .select(seatMapping.ticketId)
                        .from(eventSchedule)
                        .join(seatMapping).on(eventSchedule.eventScheduleId.eq(seatMapping.eventSchedule.eventScheduleId))
                        .where(
                                eventSchedule.event.eventId.eq(eventId)
                                        .and(seatMapping.ticketId.isNotNull())
                        )
                        .fetch()
        );
    }

    @Override
    public Page<EventScheduledOpenResponse> searchEventScheduledOpen(EventScheduledOpenSearchCond cond, Pageable pageable) {
        List<Tuple> results = queryFactory
                .select(event.eventId, event.title, event.ticketingTime, event.createdAt, event.eventImage.posterMobileUrl)
                .from(event)
                .where(
                        containsEventTitle(cond.getSearchValue()),
                        containsGenre(cond.getGenre())
                )
                .orderBy(getSortOrder(pageable))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        List<Tuple> genres = queryFactory
                .select(event.eventId, QGenre.genre)
                .from(event)
                .leftJoin(event.genre, QGenre.genre)
                .fetch();

        Map<Long, List<Genre>> genreMap = genres.stream()
                .collect(Collectors.groupingBy(
                        tuple -> tuple.get(event.eventId),
                        Collectors.mapping(tuple -> tuple.get(QGenre.genre), Collectors.toList())
                ));

        List<EventScheduledOpenResponse> content = results.stream()
                .map(tuple -> {
                    Long eventId = tuple.get(event.eventId);
                    String title = tuple.get(event.title);
                    LocalDateTime ticketingTime = tuple.get(event.ticketingTime);
                    String mobileUrl = tuple.get(event.eventImage.posterMobileUrl);
                    LocalDateTime createdAt = tuple.get(event.createdAt);
                    boolean isNewPostEvent = LocalDate.now().isEqual(createdAt.toLocalDate());

                    return new EventScheduledOpenResponse(
                            eventId,
                            title,
                            genreMap.getOrDefault(eventId, new ArrayList<>()),
                            ticketingTime,
                            mobileUrl,
                            isNewPostEvent
                    );
                })
                .toList();

        JPAQuery<Long> countQuery = queryFactory
                .select(event.count())
                .from(event)
                .where(
                        containsEventTitle(cond.getSearchValue()),
                        containsGenre(cond.getGenre())
                );

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    @Override
    public List<SimpleEvent> openSearchTop6Genre(String genre) {
        BooleanBuilder builder = new BooleanBuilder();
        LocalDateTime currentDateTime = LocalDateTime.now();
        LocalDateTime todayMidnight = currentDateTime.withHour(0).withMinute(0).withSecond(0).withNano(0);
        builder.and(event.ticketingTime.after(todayMidnight));
        // 조건 추가: genre가 포함되는 경우
        if (!genre.isEmpty()) {
            builder.and(event.genre.any().stringValue().eq(genre));
        }

        List<Event> top6 = queryFactory.selectFrom(event)
                .where(builder)
                .orderBy(event.ticketingTime.desc())
                .limit(6)
                .fetch();

        top6.sort(Comparator.comparing(Event::getTicketingTime));

        return top6.stream()
                .map(event -> {
                    return SimpleEvent.builder()
                            .eventId(event.getEventId())
                            .title(event.getTitle())
                            .date(event.getTicketingTime().toString())
                            .pcImg(event.getEventImage().getPosterPcMain2Url())
                            .mobileImg(event.getEventImage().getPosterPcMain1Url())
                            .mobileSmallImg(event.getEventImage().getPosterMobileUrl())
                            .build();
                })
                .toList();
    }

    @Override
    public List<SimpleEvent> getGenreRank(String genre) {
        BooleanBuilder builder = new BooleanBuilder();
        // 상위 10개만 가져오기
        List<Event> genreRank = queryFactory.select(event)
                .from(event)
                .leftJoin(event.eventSchedules, eventSchedule)
                .leftJoin(eventSchedule.seatMappingList, seatMapping)
                .where(
                        seatMapping.ticketId.isNotNull(),  // ticketId가 null이 아닌 것만
                        genre != null ? event.genre.any().stringValue().eq(genre) : null // genre 조건
                )
                .groupBy(event.eventId) // event 기준으로 그룹화
                .orderBy(seatMapping.ticketId.count().desc()) // ticketId 개수로 정렬
                .limit(10)
                .fetch();

        return genreRank.stream()
                .map(event -> {
                    return SimpleEvent.builder()
                            .eventId(event.getEventId())
                            .title(event.getTitle())
                            .date(event.getTicketingTime().toString())
                            .pcImg(event.getEventImage().getPosterPcMain1Url())
                            .mobileImg(event.getEventImage().getPosterPcMain2Url())
                            .build();
                })
                .toList();
    }

    @Override
    public Page<Event> findExcludingIds(List<Long> ids, Genre genre, String area, Pageable pageable) {
        JPAQuery<Event> query = queryFactory.selectFrom(event)
                .leftJoin(event.eventStage, eventStage).fetchJoin()
                .where(
                        areaIs(area),
                        genreIs(genre),
                        idNotIn(ids)
                );

        // 전체 데이터 개수 조회
        long total = query.fetchCount();

        // 페이징 적용
        List<Event> events = query
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();


        return new PageImpl<>(events, pageable, total);
    }

    @Override
    public List<Event> findIdsArea(List<Long> ids, String area) {
        JPAQuery<Event> query = queryFactory.selectFrom(event)
                .leftJoin(event.eventStage, eventStage).fetchJoin()
                .where(
                        areaIs(area),
                        event.eventId.in(ids)
                );

        return query.fetch();
    }

    private BooleanExpression containsGenre(Genre genre) {
        return genre != null ? event.genre.contains(genre) : null;
    }


    private OrderSpecifier<?>[] getSortOrder(Pageable pageable) {
        return pageable.getSort().stream()
                .map(order -> {
                    return switch (order.getProperty()) {
                        case "createdAt" -> order.isAscending() ? event.createdAt.asc() : event.createdAt.desc();
                        case "ticketingTime" ->
                                order.isAscending() ? event.ticketingTime.asc() : event.ticketingTime.desc();
                        default ->
                                throw new IllegalArgumentException("Unsupported sort property: " + order.getProperty());
                    };
                })
                .toArray(OrderSpecifier[]::new);
    }


    private BooleanExpression areaIs(String area) {
        return (area == null || area.isEmpty() || area.equals("전체")) ? null : eventStage.sido.eq(area);
    }

    private BooleanExpression genreIs(Genre genre) {
        return genre == null ? null : event.genre.contains(genre);
    }

    private BooleanExpression idNotIn(List<Long> ids) {
        return (ids == null || ids.isEmpty()) ? null : event.eventId.notIn(ids);
    }

}
