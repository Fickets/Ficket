package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.*;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.json.JsonData;
import com.example.ficketsearch.domain.search.dto.AutoCompleteRes;
import com.example.ficketsearch.domain.search.dto.Event;
import com.example.ficketsearch.domain.search.dto.SearchResult;
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
        List<Query> mustQueries = new ArrayList<>();
        int from = (pageNumber - 1) * pageSize;

        // 장르 필터
        if (isNotEmpty(genreList)) {
            mustQueries.add(Query.of(q -> q.nested(n -> n
                    .path("Genres")
                    .query(query -> query.terms(t -> t
                            .field("Genres.Genre")
                            .terms(TermsQueryField.of(tq -> tq.value(toFieldValues(genreList)))))))));
        }

        // 제목 필터
        if (title != null) {
            mustQueries.add(Query.of(q -> q.match(m -> m.field("Title").query(title))));
        } else {
            return new SearchResult(0L, 0L, Collections.emptyList());
        }

        // 지역 필터
        if (isNotEmpty(locationList)) {
            mustQueries.add(Query.of(q -> q.terms(t -> t
                    .field("Location")
                    .terms(TermsQueryField.of(tq -> tq.value(toFieldValues(locationList)))))));
        }

        // 날짜 필터
        if (startDate != null && !startDate.isEmpty() && endDate != null && !endDate.isEmpty()) {
            mustQueries.add(Query.of(q -> q.nested(n -> n
                    .path("Schedules")
                    .query(query -> query.range(r -> r
                            .field("Schedules.Schedule")
                            .gte(JsonData.of(startDate))
                            .lte(JsonData.of(endDate)))))));
        }

        // 판매 상태 필터
        if (isNotEmpty(saleTypeList)) {
            LocalDateTime now = LocalDateTime.now();
            List<Query> saleTypeQueries = saleTypeList.stream()
                    .map(saleType -> createSaleTypeQuery(saleType, now))
                    .toList();

            mustQueries.add(Query.of(q -> q.bool(b -> b.should(saleTypeQueries))));
        } else {
            return new SearchResult(0L, 0L, Collections.emptyList());
        }

        // Bool Query 생성
        Query boolQuery = Query.of(q -> q.bool(b -> b.must(mustQueries)));

        // 검색 요청 생성
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
                .index(INDEX_NAME)
                .query(boolQuery)
                .from(from)
                .size(pageSize);

        // 정렬 추가
        if (sortBy != null) {
            if (sortBy.getNestedPath() != null) {
                searchRequestBuilder.sort(s -> s.field(f -> f
                        .field(sortBy.getField())
                        .order(sortBy.getOrder())
                        .nested(n -> n.path(sortBy.getNestedPath()))));
            } else {
                searchRequestBuilder.sort(s -> s.field(f -> f
                        .field(sortBy.getField())
                        .order(sortBy.getOrder())));
            }
        }

        try {
            SearchResponse<Event> response = elasticsearchClient.search(searchRequestBuilder.build(), Event.class);

            long totalSize = response.hits().total().value();
            int totalPages = (int) Math.ceil((double) totalSize / pageSize);

            List<Event> eventList = response.hits().hits().stream()
                    .map(Hit::source)
                    .peek(event -> {
                        // 판매 상태 계산 로직 추가
                        LocalDateTime now = LocalDateTime.now();
                        if (event.getTicketing().isBefore(now)) {
                            // Schedules에서 현재 시간을 기준으로 종료 여부 확인
                            boolean isEventOver = event.getSchedules().stream()
                                    .allMatch(schedule -> LocalDateTime.parse(schedule.get("Schedule")).isBefore(now));

                            if (isEventOver) {
                                event.setSaleType(SaleType.END_OF_SALE); // 판매 종료
                            } else {
                                event.setSaleType(SaleType.ON_SALE); // 판매 중
                            }
                        } else if (event.getTicketing().isEqual(now)) {
                            // Ticketing 시간과 현재 시간이 같으면 판매 시작
                            event.setSaleType(SaleType.ON_SALE);
                        } else {
                            // Ticketing 시간이 현재 시간 이후이면 판매 예정
                            event.setSaleType(SaleType.TO_BE_SALE);
                        }
                    })
                    .toList();

            return new SearchResult(totalSize, totalPages, eventList);
        } catch (Exception e) {
            log.error("이벤트 검색 중 오류 발생: {}", e.getMessage(), e);
            return new SearchResult(0L, 0L, Collections.emptyList());
        }
    }
}
