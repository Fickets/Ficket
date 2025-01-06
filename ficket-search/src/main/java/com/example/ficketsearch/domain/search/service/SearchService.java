package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.example.ficketsearch.domain.search.dto.AutoCompleteRes;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient elasticsearchClient;

    private final static String INDEX_NAME = "event-data";


    public List<AutoCompleteRes> autoComplete(String query) {
        // Elasticsearch 클라이언트를 사용하여 검색 쿼리 구성
        try {
            SearchRequest searchRequest = new SearchRequest.Builder()
                    .index(INDEX_NAME)  // 사용할 인덱스 이름
                    .query(q -> q
                            .match(m -> m
                                    .field("Title")  // suggest 필드에서 검색
                                    .query(query)  // 사용자 입력값
                            )
                    )
                    .size(5)  // 최대 5개 결과 제한
                    .build();

            // 쿼리 실행
            SearchResponse<AutoCompleteRes> response = elasticsearchClient.search(searchRequest, AutoCompleteRes.class);

            // 검색 결과에서 AutoCompleteRes 목록 반환
            return response.hits().hits().stream()
                    .map(Hit::source)  // hit에서 source 추출하여 AutoCompleteRes로 반환
                    .collect(Collectors.toList());
        } catch (Exception e) {
            // 예외 처리: 로그 출력 및 빈 리스트 반환
            log.error("자동완성 쿼리 실행 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();  // 빈 리스트 반환
        }
    }


}
