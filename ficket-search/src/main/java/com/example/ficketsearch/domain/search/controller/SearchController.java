package com.example.ficketsearch.domain.search.controller;

import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryResponse;
import com.example.ficketsearch.domain.search.dto.AutoCompleteRes;
import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
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
     * 자동완성 검색 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-06
     * 변경 이력:
     * 2025-01-06 오형상: 초기 작성
     */
    @PostMapping("/autocomplete")
    public Mono<List<AutoCompleteRes>> autoComplete(@RequestParam("query") String query) {
        return Mono.fromFuture(() -> CompletableFuture.supplyAsync(() -> searchService.autoComplete(query)));
    }

    /**
     * 완전일치 검색 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-06
     * 변경 이력:
     * 2025-01-06 오형상: 초기 작성
     */
    // TODO 완전일치 검색 API

    /**
     * 스냅샷 복원 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-05
     * 변경 이력:
     * 2025-01-05 오형상: 초기 작성
     */
    @PostMapping("/restore")
    public void restoreSnapshot() {
        indexingService.restoreSnapshot();
    }

    /**
     * 스냅샷 저장소(S3) 초기 초기화 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-05
     * 변경 이력:
     * 2025-01-05 오형상: 초기 작성
     */
    @PutMapping("/connect-s3-elasticsearch")
    public CreateRepositoryResponse connectS3ToElasticsearch() {
        return indexingService.registerS3Repository();
    }

}
