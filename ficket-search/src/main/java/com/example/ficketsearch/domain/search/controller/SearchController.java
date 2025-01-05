package com.example.ficketsearch.domain.search.controller;

import co.elastic.clients.elasticsearch.snapshot.CreateRepositoryResponse;
import com.example.ficketsearch.domain.search.service.IndexingService;
//import com.example.ficketsearch.domain.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    //    private final SearchService searchService;
    private final IndexingService indexingService;


    @PostMapping("/restore")
    public void restoreSnapshot() {
        indexingService.restoreSnapshot();
    }

    @PutMapping("/connect-s3-elasticsearch")
    public CreateRepositoryResponse connectS3ToElasticsearch() {
        return indexingService.registerS3Repository();
    }

}
