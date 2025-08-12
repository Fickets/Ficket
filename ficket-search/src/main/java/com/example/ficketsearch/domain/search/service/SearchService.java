package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.Time;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.ScrollRequest;
import co.elastic.clients.elasticsearch.core.ScrollResponse;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.ficketsearch.domain.search.dto.*;
import com.example.ficketsearch.domain.search.enums.Genre;
import com.example.ficketsearch.domain.search.enums.Location;
import com.example.ficketsearch.domain.search.enums.SaleType;
import com.example.ficketsearch.domain.search.enums.SortBy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 검색 서비스 클래스
 * <p>
 * Elasticsearch를 활용하여 자동완성, 이벤트 검색 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;
    private static final String INDEX_NAME = "event-data";

    /**
     * 리스트가 null이 아니고 비어있지 않은지 확인합니다.
     *
     * @param list 검사할 리스트
     * @return true면 null이 아니고 비어있지 않음
     */
    private boolean isNotEmpty(List<?> list) {
        return list != null && !list.isEmpty();
    }

    /**
     * 리스트를 Elasticsearch의 FieldValue 리스트로 변환합니다.
     *
     * @param list 변환할 리스트
     * @return FieldValue 리스트
     */
    private List<FieldValue> toFieldValues(List<?> list) {
        return list.stream()
                .map(item -> FieldValue.of(f -> f.stringValue(item.toString())))
                .toList();
    }


    /**
     * 판매 상태 필터를 위한 Elasticsearch Query를 생성합니다.
     *
     * @param saleType 판매 상태
     * @param now      현재 시간
     * @return 생성된 Query
     */
    private Query createSaleTypeQuery(SaleType saleType, LocalDateTime now) {
        return switch (saleType) {
            case ON_SALE -> Query.of(q -> q.bool(b -> b
                    .must(m -> m.range(r -> r.field("Ticketing").lte(JsonData.of(now.toString())))) // Ticketing <= now
                    .must(m -> m.nested(n -> n
                            .path("Schedules")
                            .query(nq -> nq.range(r -> r.field("Schedules.Schedule").gt(JsonData.of(now.toString())))))))); // Schedules.Schedule > now
            case TO_BE_SALE -> Query.of(q -> q.range(r -> r.field("Ticketing").gt(JsonData.of(now.toString()))));
            case END_OF_SALE -> Query.of(q -> q.bool(b -> b
                    .mustNot(m -> m.nested(n -> n
                            .path("Schedules")
                            .query(nq -> nq.range(r -> r
                                    .field("Schedules.Schedule")
                                    .gt(JsonData.of(now.toString()))
                            ))
                    ))
            ));
        };
    }

    /**
     * 자동완성 기능
     *
     * @param query 사용자 입력 검색어
     * @return AutoCompleteRes 리스트 (최대 5개)
     */
    public List<AutoCompleteRes> autoComplete(String query) {
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(INDEX_NAME)
                    .query(q -> q.match(m -> m.field("Title").query(query)))
                    .size(5)
                    .build();

            SearchResponse<AutoCompleteRes> response = elasticsearchClient.search(searchRequest, AutoCompleteRes.class);

            return response.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("자동완성 쿼리 실행 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 이벤트 검색 기능
     *
     * @param title        이벤트 제목 필터
     * @param genreList    장르 필터 목록
     * @param locationList 지역 필터 목록
     * @param saleTypeList 판매 상태 필터 목록
     * @param startDate    시작 날짜 필터
     * @param endDate      종료 날짜 필터
     * @param sortBy       정렬 기준 (정확도순, 마감임박순 등)
     * @param pageNumber   페이지 번호 (1부터 시작)
     * @param pageSize     한 페이지에 보여질 데이터 개수
     * @return 검색 결과 객체(SearchResult)
     */
    public SearchResult searchEventsByFilter(String title, List<Genre> genreList, List<Location> locationList,
                                             List<SaleType> saleTypeList, String startDate, String endDate,
                                             SortBy sortBy, int pageNumber, int pageSize) {

        LocalDateTime now = LocalDateTime.now();
        Query boolQuery = buildBoolQuery(title, genreList, locationList, saleTypeList, startDate, endDate, now);

        int from = (pageNumber - 1) * pageSize;

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(boolQuery)
                .from(from)
                .size(pageSize)
                .trackTotalHits(t -> t.count(500000));

        applySort(searchRequestBuilder, sortBy);

        try {
            SearchResponse<Event> response = elasticsearchClient.search(searchRequestBuilder.build(), Event.class);

            long totalSize = response.hits().total().value();
            int totalPages = (int) Math.ceil((double) totalSize / pageSize);

            List<Event> processedEvents = processEvents(response.hits().hits().stream()
                    .map(Hit::source)
                    .toList());

            return new SearchResult(totalSize, totalPages, processedEvents);

        } catch (Exception e) {
            log.error("이벤트 검색 중 오류 발생: {}", e.getMessage(), e);
            return new SearchResult(0L, 0, Collections.emptyList());
        }
    }

    public ScrollSearchResult searchEventsByFilterWithScroll(String title, List<Genre> genreList, List<Location> locationList,
                                                             List<SaleType> saleTypeList, String startDate, String endDate,
                                                             SortBy sortBy, int scrollSize, String scrollId) {

        LocalDateTime now = LocalDateTime.now();
        Query boolQuery = buildBoolQuery(title, genreList, locationList, saleTypeList, startDate, endDate, now);

        try {
            if (scrollId == null || scrollId.isEmpty()) {
                SearchRequest.Builder builder = new SearchRequest.Builder()
                        .index(INDEX_NAME)
                        .query(boolQuery)
                        .size(scrollSize)
                        .scroll(Time.of(t -> t.time("1m")));

                applySort(builder, sortBy);

                SearchResponse<Event> response = elasticsearchClient.search(builder.build(), Event.class);

                List<Event> processedEvents = processEvents(response.hits().hits().stream()
                        .map(Hit::source)
                        .toList());

                long total = response.hits().total().value();

                return new ScrollSearchResult(total, processedEvents, response.scrollId());
            } else {
                ScrollRequest scrollRequest = new ScrollRequest.Builder()
                        .scrollId(scrollId)
                        .scroll(Time.of(t -> t.time("1m")))
                        .build();

                ScrollResponse<Event> scrollResponse = elasticsearchClient.scroll(scrollRequest, Event.class);

                List<Event> processedEvents = processEvents(scrollResponse.hits().hits().stream()
                        .map(Hit::source)
                        .toList());

                return new ScrollSearchResult(null, processedEvents, scrollResponse.scrollId());
            }
        } catch (Exception e) {
            log.error("Scroll 검색 중 오류 발생: {}", e.getMessage(), e);
            return new ScrollSearchResult(0L, Collections.emptyList(), null);
        }
    }

    public SearchAfterResult searchEventsByFilterWithSearchAfter(String title, List<Genre> genreList,
                                                                 List<Location> locationList,
                                                                 List<SaleType> saleTypeList, String startDate,
                                                                 String endDate, SortBy sortBy,
                                                                 List<Object> searchAfter, int pageSize) {

        LocalDateTime now = LocalDateTime.now();
        Query boolQuery = buildBoolQuery(title, genreList, locationList, saleTypeList, startDate, endDate, now);

        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(boolQuery)
                .size(pageSize + 1)
                .trackTotalHits(t -> t.enabled(true));

        applySort(searchRequestBuilder, sortBy);

        // tie-breaker 정렬 (항상 추가)
        searchRequestBuilder.sort(s -> s.field(f -> f
                .field("EventId.keyword")
                .order(SortOrder.Asc)));

        if (searchAfter != null && !searchAfter.isEmpty()) {
            List<FieldValue> searchAfterFieldValues = searchAfter.stream()
                    .map(this::convertObjectToFieldValue)
                    .toList();
            searchRequestBuilder.searchAfter(searchAfterFieldValues);
        }

        try {
            SearchResponse<Event> response = elasticsearchClient.search(searchRequestBuilder.build(), Event.class);

            List<Hit<Event>> hits = response.hits().hits();

            boolean hasNext = false;
            if (hits.size() > pageSize) {
                hasNext = true;
                hits = hits.subList(0, pageSize);
            }

            List<Event> processedEvents = processEvents(hits.stream()
                    .map(Hit::source)
                    .toList());

            List<Object> nextSearchAfter = null;
            if (hasNext && !processedEvents.isEmpty()) {
                Hit<Event> lastHit = hits.get(hits.size() - 1);
                nextSearchAfter = lastHit.sort().stream()
                        .map(this::convertFieldValueToObject)
                        .toList();
            }

            Long totalSize = response.hits().total() != null ?
                    response.hits().total().value() : 0L;

            return new SearchAfterResult(totalSize, processedEvents, nextSearchAfter);
        } catch (Exception e) {
            log.error("이벤트 검색 중 오류 발생: {}", e.getMessage(), e);
            return new SearchAfterResult(0L, Collections.emptyList(), null);
        }
    }

    private Query buildBoolQuery(String title, List<Genre> genreList, List<Location> locationList,
                                 List<SaleType> saleTypeList, String startDate, String endDate, LocalDateTime now) {
        List<Query> mustQueries = new ArrayList<>();

        if (isNotEmpty(genreList)) {
            mustQueries.add(Query.of(q -> q.nested(n -> n
                    .path("Genres")
                    .query(query -> query.terms(t -> t
                            .field("Genres.Genre")
                            .terms(TermsQueryField.of(tq -> tq.value(toFieldValues(genreList)))))))));
        }

        if (title != null && !title.trim().isEmpty()) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("Title").query(title))));
        }

        if (isNotEmpty(locationList)) {
            mustQueries.add(Query.of(q -> q.terms(t -> t
                    .field("Location")
                    .terms(TermsQueryField.of(tq -> tq.value(toFieldValues(locationList)))))));
        }

        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            mustQueries.add(Query.of(q -> q.nested(n -> n
                    .path("Schedules")
                    .query(query -> query.range(r -> r
                            .field("Schedules.Schedule")
                            .gte(JsonData.of(startDate))
                            .lte(JsonData.of(endDate)))))));
        }

        if (isNotEmpty(saleTypeList)) {
            List<Query> saleTypeQueries = saleTypeList.stream()
                    .map(saleType -> createSaleTypeQuery(saleType, now))
                    .toList();

            mustQueries.add(Query.of(q -> q.bool(b -> b.should(saleTypeQueries))));
        }

        return Query.of(q -> q.bool(b -> b.must(mustQueries)));
    }

    private void calculateSaleType(Event event) {
        LocalDateTime now = LocalDateTime.now();
        if (event.getTicketing().isBefore(now)) {
            boolean isEventOver = event.getSchedules().stream()
                    .allMatch(schedule -> LocalDateTime.parse(schedule.get("Schedule")).isBefore(now));
            event.setSaleType(isEventOver ? SaleType.END_OF_SALE : SaleType.ON_SALE);
        } else if (event.getTicketing().isEqual(now)) {
            event.setSaleType(SaleType.ON_SALE);
        } else {
            event.setSaleType(SaleType.TO_BE_SALE);
        }
    }

    private List<Event> processEvents(List<Event> events) {
        return events.stream()
                .peek(this::calculateSaleType)
                .toList();
    }

    private void applySort(SearchRequest.Builder builder, SortBy sortBy) {
        if (sortBy == null) return;

        if (sortBy.getNestedPath() != null) {
            builder.sort(s -> s.field(f -> f
                    .field(sortBy.getField())
                    .order(sortBy.getOrder())
                    .nested(n -> n.path(sortBy.getNestedPath()))));
        } else {
            builder.sort(s -> s.field(f -> f
                    .field(sortBy.getField())
                    .order(sortBy.getOrder())));
        }
    }

    private FieldValue convertObjectToFieldValue(Object obj) {
        if (obj == null) {
            return FieldValue.NULL;
        } else if (obj instanceof String) {
            return FieldValue.of((String) obj);
        } else if (obj instanceof Long) {
            return FieldValue.of((Long) obj);
        } else if (obj instanceof Integer) {
            return FieldValue.of(((Integer) obj).longValue());
        } else if (obj instanceof Double) {
            return FieldValue.of((Double) obj);
        } else if (obj instanceof Float) {
            return FieldValue.of(((Float) obj).doubleValue());
        } else if (obj instanceof Boolean) {
            return FieldValue.of((Boolean) obj);
        } else {
            // 숫자 타입 처리 (JSON에서 파싱된 경우)
            try {
                if (obj.toString().contains(".")) {
                    return FieldValue.of(Double.parseDouble(obj.toString()));
                } else {
                    return FieldValue.of(Long.parseLong(obj.toString()));
                }
            } catch (NumberFormatException e) {
                return FieldValue.of(obj.toString());
            }
        }
    }

    // FieldValue를 Object로 변환하는 헬퍼 메서드
    private Object convertFieldValueToObject(FieldValue fieldValue) {
        if (fieldValue.isString()) {
            return fieldValue.stringValue();
        } else if (fieldValue.isLong()) {
            return fieldValue.longValue();
        } else if (fieldValue.isDouble()) {
            return fieldValue.doubleValue();
        } else if (fieldValue.isBoolean()) {
            return fieldValue.booleanValue();
        } else if (fieldValue.isNull()) {
            return null;
        } else {
            // 기본적으로 toString() 사용
            return fieldValue.toString();
        }
    }

}
