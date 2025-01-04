package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import com.example.ficketsearch.config.utils.CsvToBulkApiConverter;
import com.example.ficketsearch.config.utils.S3Utils;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingService {

    private final S3Utils s3Utils;
    private final CsvToBulkApiConverter csvToBulkApiConverter;
    private final ElasticsearchClient elasticsearchClient;
    private final ObjectMapper objectMapper;

    private static final int BULK_SIZE = 2000;
    private static final String INDEX_NAME = "event-data";
    private static final String ALIAS_NAME = "ficket-search";

    /**
     * 인덱스를 생성하는 메서드로, Edge Ngram 설정을 포함한 인덱스를 생성합니다.
     * 인덱스 생성 시 분석기 및 매핑 설정을 사용하여 인덱스를 생성합니다.
     */
    private void createIndexWithEdgeNgram() {
        try {
            IndexSettings indexSettings = new IndexSettings.Builder()
                    .numberOfShards("5")
                    .numberOfReplicas("1")
                    .analysis(createAnalysis())  // 분석기 설정
                    .index(new IndexSettings.Builder().maxNgramDiff(4).build())
                    .build();

            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(INDEX_NAME)
                    .aliases(ALIAS_NAME, new Alias.Builder().isWriteIndex(false).build())
                    .settings(indexSettings)
                    .mappings(createMappings()) // 매핑 설정
                    .build();

            CreateIndexResponse createIndexResponse = elasticsearchClient.indices().create(createIndexRequest);
            log.info("Edge Ngram 설정을 포함한 인덱스가 생성되었습니다: {}", createIndexResponse.index());
        } catch (Exception e) {
            log.error("Edge Ngram 설정을 포함한 인덱스 생성에 실패했습니다: {}", e.getMessage(), e);
        }
    }

    /**
     * Elasticsearch에 사용할 매핑을 생성하는 메서드
     * Title, Genre, Schedules 등 다양한 필드의 데이터 유형을 설정합니다.
     *
     * @return TypeMapping - 필드 매핑 설정
     */
    private TypeMapping createMappings() {
        return new TypeMapping.Builder()
                .properties("Title", p -> p.text(t -> t.analyzer("ngram_analyzer").searchAnalyzer("remove_whitespace_analyzer")))
                .properties("Title_Keyword", p -> p.keyword(k -> k))
                .properties("Stage", p -> p.text(t -> t.analyzer("ngram_analyzer").searchAnalyzer("remove_whitespace_analyzer")))
                .properties("Stage_Keyword", p -> p.keyword(k -> k))
                .properties("Genres", p -> p.nested(n -> n.properties("Genre", pr -> pr.keyword(k -> k))))
                .properties("Location", p -> p.keyword(k -> k))
                .properties("Schedules", p -> p.nested(n -> n.properties("Schedule", pr -> pr.date(d -> d))))
                .properties("Ticketing", p -> p.date(d -> d))
                .build();
    }

    /**
     * 분석기 설정을 생성하는 메서드
     * Edge Ngram을 사용하여 검색어 완성 및 필터링 작업을 처리합니다.
     *
     * @return IndexSettingsAnalysis - 분석기 설정
     */
    private IndexSettingsAnalysis createAnalysis() {
        return new IndexSettingsAnalysis.Builder()
                .charFilter("remove_whitespace", new CharFilter.Builder().definition(def -> def.patternReplace(r -> r.pattern("\\s+").replacement(""))).build())
                .tokenizer("ngram_tokenizer", new Tokenizer.Builder().definition(def -> def.ngram(new NGramTokenizer.Builder().minGram(1).maxGram(5).tokenChars(TokenChar.Letter, TokenChar.Digit).build())).build())
                .analyzer("ngram_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("ngram_tokenizer").filter("lowercase").charFilter("remove_whitespace")).build())
                .analyzer("remove_whitespace_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("standard").charFilter("remove_whitespace")).build())
                .build();
    }

    /**
     * 전체 색인 처리를 위한 메서드
     * 주어진 S3 URL에서 CSV 파일을 다운로드하고, 이를 Elasticsearch에 삽입합니다.
     *
     * @param s3Url - S3에서 파일을 다운로드할 URL
     */
    public void handleFullIndexing(String s3Url) {
        createIndexWithEdgeNgram(); // 인덱스 생성

        String downloadPath = s3Utils.downloadFile(s3Url); // S3에서 파일 다운로드

        try (Stream<String> bulkJsonStream = csvToBulkApiConverter.convertCsvToBulkJsonStream(downloadPath, INDEX_NAME)) {
            insertDataToElasticsearch(bulkJsonStream); // Elasticsearch에 데이터 삽입
        } catch (Exception e) {
            log.error("전체 색인 처리 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Elasticsearch에 데이터를 삽입하는 메서드
     * CSV 데이터를 벌크로 처리하여 Elasticsearch에 삽입합니다.
     *
     * @param bulkJsonStream - CSV 데이터를 JSON 형태로 변환한 스트림
     */
    private void insertDataToElasticsearch(Stream<String> bulkJsonStream) {
        List<String> currentBatch = new ArrayList<>();
        bulkJsonStream.forEach(jsonLine -> {
            currentBatch.add(jsonLine);
            if (currentBatch.size() >= BULK_SIZE) {
                processBatch(currentBatch); // 벌크 데이터 처리
                currentBatch.clear();
            }
        });
        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch); // 마지막 배치 처리
        }
    }

    /**
     * 벌크 데이터를 처리하는 메서드
     * 주어진 데이터 배치를 Elasticsearch에 삽입합니다.
     *
     * @param batch - 처리할 데이터 배치
     */
    private void processBatch(List<String> batch) {
        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (String jsonLine : batch) {
                JsonNode jsonNode = objectMapper.readTree(jsonLine.split("\n")[1]);

                // 필드 처리: Genres, Schedules가 문자열이면 JSON 배열로 변환
                transformJsonArrayField(jsonNode, "Genres");
                transformJsonArrayField(jsonNode, "Schedules");

                BulkOperation operation = BulkOperation.of(b -> b.index(i -> i.index(INDEX_NAME).document(jsonNode)));
                operations.add(operation);
            }

            BulkRequest bulkRequest = new BulkRequest.Builder().operations(operations).build();
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

            if (bulkResponse.errors()) {
                bulkResponse.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("벌크 작업 실패: {}", item.error().reason());
                    }
                });
            } else {
                log.info("Bulk API 요청 성공, 배치 크기: {}", batch.size());
            }
        } catch (Exception e) {
            log.error("벌크 요청 실패, 배치 크기: {}", batch.size(), e);
            throw new RuntimeException("Bulk 삽입 실패", e);
        }
    }

    /**
     * 주어진 필드를 JSON 배열로 변환하는 메서드
     * 해당 필드가 문자열이면, 이를 JSON 배열로 변환하여 저장합니다.
     *
     * @param jsonNode  - 변환할 JSON 데이터
     * @param fieldName - 변환할 필드 이름
     */
    private void transformJsonArrayField(JsonNode jsonNode, String fieldName) {
        if (jsonNode.has(fieldName) && jsonNode.get(fieldName).isTextual()) {
            try {
                String fieldString = jsonNode.get(fieldName).asText();
                JsonNode fieldArray = objectMapper.readTree(fieldString);
                ((ObjectNode) jsonNode).set(fieldName, fieldArray);
            } catch (Exception e) {
                log.error("{} 필드를 JSON 배열로 변환하는데 실패했습니다", fieldName, e);
            }
        }
    }

    /**
     * Elasticsearch에 문서를 생성하는 메서드
     *
     * @param map - 삽입할 문서 데이터
     */
    public void handlePartialIndexingCreate(Map<String, Object> map) {
        try {
            String eventId = (String) map.get("EventId");
            IndexRequest<Map<String, Object>> request = IndexRequest.of(builder -> builder.index(INDEX_NAME).document(map));
            IndexResponse response = elasticsearchClient.index(request);
            log.info("Elasticsearch에 문서가 생성되었습니다: {}, 결과: {}", eventId, response.result());
        } catch (Exception e) {
            log.error("Elasticsearch에 문서 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("문서 생성 실패", e);
        }
    }

    /**
     * Elasticsearch에서 문서를 업데이트하는 메서드
     *
     * @param map - 업데이트할 문서 데이터
     */
    public void handlePartialIndexingUpdate(Map<String, Object> map) {
        try {
            String eventId = (String) map.get("EventId");
            if (eventId == null) throw new IllegalArgumentException("EventId가 없습니다");

            SearchRequest searchRequest = SearchRequest.of(builder -> builder.index(INDEX_NAME).query(q -> q.term(t -> t.field("EventId").value(eventId))));
            SearchResponse<Map> searchResponse = elasticsearchClient.search(searchRequest, Map.class);

            if (searchResponse.hits().hits().isEmpty()) {
                throw new RuntimeException("EventId로 문서를 찾을 수 없습니다: " + eventId);
            }

            String documentId = searchResponse.hits().hits().get(0).id();
            IndexRequest<Map<String, Object>> indexRequest = IndexRequest.of(builder -> builder.index(INDEX_NAME).id(documentId).document(map));
            IndexResponse response = elasticsearchClient.index(indexRequest);

            log.info("Elasticsearch에 문서가 업데이트되었습니다: EventId: {}, 응답: {}", eventId, response.result());
        } catch (Exception e) {
            log.error("Elasticsearch에서 문서 업데이트 실패: {}", e.getMessage(), e);
            throw new RuntimeException("문서 업데이트 실패", e);
        }
    }

    /**
     * Elasticsearch에서 문서를 삭제하는 메서드
     *
     * @param eventId - 삭제할 문서의 EventId
     */
    public void handlePartialIndexingDelete(String eventId) {
        try {
            DeleteByQueryRequest request = DeleteByQueryRequest.of(builder -> builder.index(INDEX_NAME).query(q -> q.term(t -> t.field("EventId").value(eventId))));
            long deletedCount = elasticsearchClient.deleteByQuery(request).deleted();
            log.info("EventId: {}에 해당하는 문서가 삭제되었습니다. 삭제된 문서 수: {}", eventId, deletedCount);
        } catch (Exception e) {
            log.error("EventId: {}에 해당하는 문서 삭제 실패: {}", eventId, e.getMessage(), e);
            throw new RuntimeException("문서 삭제 실패", e);
        }
    }

}
