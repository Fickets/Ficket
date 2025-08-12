package com.example.ficketsearch.domain.search.controller;

import com.example.ficketsearch.domain.search.dto.AutoCompleteRes;
import com.example.ficketsearch.domain.search.dto.ScrollSearchResult;
import com.example.ficketsearch.domain.search.dto.SearchAfterResult;
import com.example.ficketsearch.domain.search.dto.SearchResult;
import com.example.ficketsearch.domain.search.enums.Genre;
import com.example.ficketsearch.domain.search.enums.Location;
import com.example.ficketsearch.domain.search.enums.SaleType;
import com.example.ficketsearch.domain.search.enums.SortBy;
import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final IndexingService indexingService;
    private final ObjectMapper objectMapper;


    /**
     * 자동 완성 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-06
     * 변경 이력:
     * 2025-01-06 오형상: 초기 작성
     * 2025-08-07 오형상: Spring WebFlux -> Spring Web 전환
     */
    @GetMapping("/auto-complete")
    public ResponseEntity<List<AutoCompleteRes>> autoComplete(@RequestParam("query") String query) {
        List<AutoCompleteRes> autoCompleteRes = searchService.autoComplete(query);
        return ResponseEntity.ok(autoCompleteRes);
    }

    /**
     * 검색 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-06
     * 변경 이력:
     * 2025-01-06 오형상: 초기 작성
     * 2025-08-07 오형상: Spring WebFlux -> Spring Web 전환
     * 2025-08-07 오형상: title, SaleType 조건 없으면 빈 리스트 반환 로직 추가 및 관련 쿼리 조건 수정
     */
    @GetMapping("/detail")
    public ResponseEntity<SearchResult> searchByFilter(
            @RequestParam String title,
            @RequestParam(required = false) List<Genre> genreList,
            @RequestParam(required = false) List<Location> locationList,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) List<SaleType> saleTypeList,
            @RequestParam(defaultValue = "SORT_BY_ACCURACY") SortBy sortBy,
            @RequestParam(defaultValue = "1") int pageNumber,
            @RequestParam(defaultValue = "20") int pageSize
    ) {
        SearchResult searchResult = searchService.searchEventsByFilter(title, genreList, locationList, saleTypeList, startDate, endDate, sortBy, pageNumber, pageSize);
        return ResponseEntity.ok(searchResult);
    }

    @GetMapping("/detail2")
    public ResponseEntity<ScrollSearchResult> searchWithScroll(
            @RequestParam String title,
            @RequestParam(required = false) List<Genre> genreList,
            @RequestParam(required = false) List<Location> locationList,
            @RequestParam(required = false) List<SaleType> saleTypeList,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam SortBy sortBy,
            @RequestParam(defaultValue = "20") int scrollSize,
            @RequestParam(required = false) String scrollId
    ) {
        return ResponseEntity.ok(searchService.searchEventsByFilterWithScroll(
                title, genreList, locationList, saleTypeList, startDate, endDate, sortBy, scrollSize, scrollId));
    }

    @GetMapping("/detail3")
    public ResponseEntity<SearchAfterResult> searchWithSearchAfter(
            @RequestParam String title,
            @RequestParam(required = false) List<Genre> genreList,
            @RequestParam(required = false) List<Location> locationList,
            @RequestParam(required = false) List<SaleType> saleTypeList,
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            @RequestParam SortBy sortBy,
            @RequestParam(required = false) String searchAfter,
            @RequestParam(defaultValue = "20") int pageSize) {

        try {
            // searchAfter 파라미터 파싱
            List<Object> searchAfterList = parseSearchAfter(searchAfter);

            // 페이지 사이즈 제한
            pageSize = Math.min(Math.max(pageSize, 1), 100);

            SearchAfterResult result = searchService.searchEventsByFilterWithSearchAfter(
                    title, genreList, locationList, saleTypeList,
                    startDate, endDate, sortBy, searchAfterList, pageSize);

            return ResponseEntity.ok(result);

        } catch (Exception e) {
            log.error("검색 API 호출 중 오류: ", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new SearchAfterResult(0L, Collections.emptyList(), Collections.emptyList()));
        }
    }

    /**
     * 스냅샷 복원 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-05
     * 변경 이력:
     * 2025-01-05 오형상: 초기 작성
     * 2025-08-07 오형상: Spring WebFlux -> Spring Web 전환
     */
    @PostMapping("/restore")
    public ResponseEntity<Void> restoreSnapshot() {
        indexingService.restoreSnapshot();
        return ResponseEntity.noContent().build();
    }

//    /**
//     * 스냅샷 저장소(S3) 초기 초기화 API
//     * <p>
//     * 작업자: 오형상
//     * 작업 날짜: 2025-01-05
//     * 변경 이력:
//     * 2025-01-05 오형상: 초기 작성
//     */
//    @PutMapping("/connect-s3-elasticsearch")
//    public CreateRepositoryResponse connectS3ToElasticsearch() {
//        return indexingService.registerS3Repository();
//    }


    private List<Object> parseSearchAfter(String searchAfter) {
        if (!StringUtils.hasText(searchAfter)) {
            return null;
        }

        try {
            // JSON 배열 문자열을 List<Object>로 파싱
            return objectMapper.readValue(searchAfter,
                    objectMapper.getTypeFactory().constructCollectionType(List.class, Object.class));
        } catch (Exception e) {
            log.warn("searchAfter 파싱 실패: {}", searchAfter, e);
            return null;
        }
    }

}
