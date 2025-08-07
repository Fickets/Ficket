package com.example.ficketsearch.domain.search.controller;

import com.example.ficketsearch.domain.search.dto.AutoCompleteRes;
import com.example.ficketsearch.domain.search.dto.SearchResult;
import com.example.ficketsearch.domain.search.enums.Genre;
import com.example.ficketsearch.domain.search.enums.Location;
import com.example.ficketsearch.domain.search.enums.SaleType;
import com.example.ficketsearch.domain.search.enums.SortBy;
import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;
    private final IndexingService indexingService;


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
            @RequestParam(required = false) String title,
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

}
