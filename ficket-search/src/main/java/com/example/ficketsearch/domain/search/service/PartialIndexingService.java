package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.*;
import com.example.ficketsearch.domain.search.dto.PartialIndexingUpsertDto;
import com.example.ficketsearch.domain.search.dto.PartialIndexingDeleteDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartialIndexingService {

    private final ElasticsearchClient elasticsearchClient;

    private static final String INDEX_NAME = "event-data";

    /**
     * Elasticsearch에서 문서를 생성/수정 하는 메서드
     */
    public void upsertDocument(PartialIndexingUpsertDto dto) {
        try {
            UpdateRequest<PartialIndexingUpsertDto, PartialIndexingUpsertDto> request = UpdateRequest.of(builder ->
                    builder.index(INDEX_NAME)
                            .id(dto.getEventId())
                            .doc(dto)
                            .docAsUpsert(true)
            );

            elasticsearchClient.update(request, PartialIndexingUpsertDto.class);
            log.info("[PARTIAL-UPSERT] 문서 생성/부분 업데이트 완료: EventId={}", dto.getEventId());

        } catch (Exception e) {
            log.error("[PARTIAL-UPSERT] 문서 생성/업데이트 실패: EventId={}", dto.getEventId(), e);
            throw new RuntimeException("부분 색인(upsert) 실패", e);
        }
    }

    /**
     * Elasticsearch에서 문서를 삭제하는 메서드
     */
    public void deleteDocument(PartialIndexingDeleteDto dto) {
        try {

            DeleteRequest request = DeleteRequest.of(builder ->
                    builder.index(INDEX_NAME)
                            .id(dto.getEventId())
            );

            elasticsearchClient.delete(request);
            log.info("[DELETE] 문서 삭제 완료");

        } catch (Exception e) {
            log.error("[PARTIAL-DELETE] 삭제 실패", e);
            throw new RuntimeException("부분 색인(DELETE) 실패", e);
        }
    }

}
