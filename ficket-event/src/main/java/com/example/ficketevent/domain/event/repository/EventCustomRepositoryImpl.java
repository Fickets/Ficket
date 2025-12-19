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

@Slf4j
@RequiredArgsConstructor
public class EventCustomRepositoryImpl implements EventCustomRepository {

    private final JPAQueryFactory queryFactory;

    private BooleanExpression containsEventTitle(String eventTitle) {
        return eventTitle != null ? event.title.containsIgnoreCase(eventTitle) : null;
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
        return queryFactory
                .selectFrom(event)
                .join(event.eventImage).fetchJoin()
                .where(
                        ticketingTimeAfterToday(),
                        eqGenre(genre)
                )
                .orderBy(event.ticketingTime.asc())
                .limit(6)
                .fetch()
                .stream()
                .map(SimpleEvent::from)
                .toList();
    }

    private BooleanExpression ticketingTimeAfterToday() {
        return event.ticketingTime.after(LocalDate.now().atStartOfDay());
    }

    private BooleanExpression eqGenre(String genre) {
        if (genre == null || genre.isBlank()) {
            return null;
        }
        return event.genre.any().stringValue().eq(genre);
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
