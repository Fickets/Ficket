package com.example.ficketsearch.domain.search.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch._types.analysis.*;
import co.elastic.clients.elasticsearch._types.mapping.TypeMapping;
import co.elastic.clients.elasticsearch.core.*;
import co.elastic.clients.elasticsearch.core.bulk.BulkOperation;
import co.elastic.clients.elasticsearch.indices.*;
import co.elastic.clients.elasticsearch.snapshot.*;
import co.elastic.clients.util.ApiTypeHelper;
import com.example.ficketsearch.global.config.awsS3.S3Constants;
import com.example.ficketsearch.global.config.elasticsearch.ElasticsearchConstants;
import com.example.ficketsearch.global.config.utils.CsvToBulkApiConverter;
import com.example.ficketsearch.global.config.utils.FileUtils;
import com.example.ficketsearch.global.config.utils.S3Utils;
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
public class FullIndexingService {

    private final S3Utils s3Utils;
    private final CsvToBulkApiConverter csvToBulkApiConverter;
    private final ElasticsearchClient elasticsearchClient;

    /**
     * 인덱스를 생성하는 메서드로, Edge Ngram 설정을 포함한 인덱스를 생성합니다.
     * 인덱스 생성 시 분석기 및 매핑 설정을 사용하여 인덱스를 생성합니다.
     */
    private void createIndexWithEdgeNgram() {
        try {
            IndexSettings indexSettings = new IndexSettings.Builder()
                    .numberOfShards("1")
                    .numberOfReplicas("0")
                    .analysis(createAnalysis())  // 분석기 설정
                    .index(new IndexSettings.Builder().maxNgramDiff(29).build())
                    .build();

            CreateIndexRequest createIndexRequest = new CreateIndexRequest.Builder()
                    .index(ElasticsearchConstants.INDEX_NAME.toString())
                    .aliases(ElasticsearchConstants.ALIAS_NAME.toString(), new Alias.Builder().isWriteIndex(false).build())
                    .settings(indexSettings)
                    .mappings(createMappings())
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
                .charFilter("remove_special_characters", new CharFilter.Builder().definition(def -> def.patternReplace(r -> r.pattern("[^\\w\\d]+").replacement(""))).build())  // 특수문자 제거
                .tokenizer("ngram_tokenizer", new Tokenizer.Builder().definition(def -> def.ngram(new NGramTokenizer.Builder().minGram(1).maxGram(30).tokenChars(TokenChar.Letter, TokenChar.Digit).build())).build())
                .analyzer("ngram_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("ngram_tokenizer").filter("lowercase").charFilter("remove_whitespace")).build())
                .analyzer("remove_whitespace_analyzer", new Analyzer.Builder().custom(custom -> custom.tokenizer("standard").charFilter("remove_whitespace")).build())
                .build();
    }

    /**
     * 전체 색인 처리를 위한 메서드
     * 주어진 S3 URL에서 CSV 파일을 다운로드하고, 이를 Elasticsearch에 삽입합니다.
     *
     * @param payload - S3에서 파일을 다운로드할 URL List
     */
    public void handleFullIndexing(String payload) {
        String[] s3UrlList = payload.split(",");

        for (String s3Url : s3UrlList) {
            String downloadPath = s3Utils.downloadFileWithRetry(s3Url); // S3에서 파일 다운로드

            try (Stream<Map<String, Object>> bulkJsonStream = csvToBulkApiConverter.convertCsvToJsonStream(downloadPath)) {
                insertDataToElasticsearch(bulkJsonStream); // Elasticsearch에 데이터 삽입
            } catch (Exception e) {
                log.error("전체 색인 처리 중 오류 발생: {}", e.getMessage(), e);
            } finally {
                FileUtils.deleteFile(downloadPath);
            }
        }
    }

    public void initializeIndexing() {

        registerS3Repository(); // S3 저장소 설정
        deleteExistingSnapshot(); // 기존 스냅샷 삭제
        backupCurrentData(); // 현재 상태를 스냅샷으로 S3에 저장
        deleteExistingData(); // 기존 데이터 삭제
        createIndexIfNotExist(); // 인덱스 생성

    }

    /**
     * 인덱스가 존재하지 않을 경우, Edge Ngram 분석기가 포함된 인덱스를 생성합니다.
     */
    private void createIndexIfNotExist() {
        try {
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(ElasticsearchConstants.INDEX_NAME.toString())).value();
            if (!exists) {
                createIndexWithEdgeNgram();
            }
        } catch (Exception e) {
            log.error("인덱스 생성 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * Elasticsearch에 데이터를 삽입하는 메서드
     * 주어진 JSON 스트림을 일정 크기(BULK_SIZE)로 나누어 Elasticsearch에 벌크 삽입합니다.
     *
     * @param bulkJsonStream - CSV 데이터를 JSON 형태로 변환한 스트림
     */
    private void insertDataToElasticsearch(Stream<Map<String, Object>> bulkJsonStream) {
        List<Map<String, Object>> currentBatch = new ArrayList<>(ElasticsearchConstants.BULK_SIZE.toInt());

        bulkJsonStream.forEach(jsonMap -> {
            currentBatch.add(jsonMap);
            if (currentBatch.size() >= ElasticsearchConstants.BULK_SIZE.toInt()) {
                processBatch(currentBatch);
                currentBatch.clear();
            }
        });

        if (!currentBatch.isEmpty()) {
            processBatch(currentBatch);
        }
    }

    /**
     * 한 배치 내 문서들을 Elasticsearch Bulk API로 색인합니다. 유효하지 않은 문서(EventId 누락)는 제외합니다.
     *
     * @param batch - 처리할 데이터 배치
     */
    private void processBatch(List<Map<String, Object>> batch) {
        try {
            List<BulkOperation> operations = new ArrayList<>();
            for (Map<String, Object> document : batch) {
                String id = (String) document.get("EventId");
                if (id == null || id.isBlank()) {
                    log.warn("EventId가 없는 문서를 스킵합니다: {}", document);
                    continue;
                }

                BulkOperation operation = BulkOperation.of(b -> b
                        .index(i -> i
                                .index(ElasticsearchConstants.INDEX_NAME.toString())
                                .id(id)
                                .document(document)
                        )
                );
                operations.add(operation);
            }

            if (operations.isEmpty()) return;

            BulkRequest bulkRequest = new BulkRequest.Builder().operations(operations).build();
            BulkResponse bulkResponse = elasticsearchClient.bulk(bulkRequest);

            if (bulkResponse.errors()) {
                bulkResponse.items().forEach(item -> {
                    if (item.error() != null) {
                        log.error("벌크 작업 실패: {}", item.error().reason());
                    }
                });
            }
        } catch (Exception e) {
            log.error("벌크 요청 실패, 배치 크기: {}", batch.size(), e);
            throw new RuntimeException("Bulk 삽입 실패", e);
        }
    }

    /**
     * 기존 스냅샷을 삭제하는 메서드
     * 색인 작업 전 이전 스냅샷을 삭제합니다.
     */
    private void deleteExistingSnapshot() {
        try {
            DeleteSnapshotRequest deleteSnapshotRequest = new DeleteSnapshotRequest.Builder()
                    .repository(ElasticsearchConstants.SNAPSHOT_STORAGE_NAME.toString())
                    .snapshot(ElasticsearchConstants.SNAPSHOT_NAME.toString())
                    .build();

            elasticsearchClient.snapshot().delete(deleteSnapshotRequest);
            log.info("기존 스냅샷이 삭제되었습니다.");
        } catch (ElasticsearchException e) {
            log.info("삭제할 스냅샷이 존재하지 않아 삭제하지 않았습니다.");
        } catch (Exception e) {
            log.error("스냅샷 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    /**
     * 현재 상태를 스냅샷으로 S3에 백업하는 메서드
     * 현재 상태를 스냅샷으로 저장소에 백업합니다.
     */
    private void backupCurrentData() {
        try {
            // 백업할 인덱스가 존재하는지 확인
            GetIndexRequest getIndexRequest = new GetIndexRequest.Builder()
                    .index(ElasticsearchConstants.INDEX_NAME.toString())  // 확인할 인덱스 이름
                    .build();

            // 인덱스가 존재하지 않으면 백업하지 않음
            try {
                elasticsearchClient.indices().get(getIndexRequest);
            } catch (Exception e) {
                log.error("백업할 인덱스가 존재하지 않습니다");
                return;
            }

            // 스냅샷을 생성
            CreateSnapshotRequest snapshotRequest = new CreateSnapshotRequest.Builder()
                    .repository(ElasticsearchConstants.SNAPSHOT_STORAGE_NAME.toString())  // 저장소 이름
                    .snapshot(ElasticsearchConstants.SNAPSHOT_NAME.toString())     // 스냅샷 이름
                    .indices(ElasticsearchConstants.INDEX_NAME.toString())            // 백업할 인덱스
                    .build();

            elasticsearchClient.snapshot().create(snapshotRequest);
            log.info("현재 상태의 스냅샷이 생성되어 백업되었습니다.");
        } catch (Exception e) {
            log.error("스냅샷 생성 중 오류 발생: {}", e.getMessage(), e);
            // 예외를 던지지 않고, 계속 진행하도록 합니다.
        }
    }

    /**
     * 기존 데이터를 삭제하는 메서드
     * 전체 색인 작업 전, 기존 데이터를 삭제합니다.
     */
    private void deleteExistingData() {
        try {
            // 인덱스 존재 여부 먼저 확인
            boolean exists = elasticsearchClient.indices().exists(e -> e.index(ElasticsearchConstants.INDEX_NAME.toString())).value();
            if (!exists) {
                log.info("삭제할 인덱스가 존재하지 않아 삭제를 생략합니다");
                return;
            }

            // 인덱스가 존재할 경우 삭제 진행
            DeleteByQueryRequest deleteRequest = new DeleteByQueryRequest.Builder()
                    .index(ElasticsearchConstants.INDEX_NAME.toString())
                    .query(q -> q.matchAll(m -> m))
                    .build();

            long deletedCount = elasticsearchClient.deleteByQuery(deleteRequest).deleted();

            if (deletedCount > 0) {
                log.info("기존 데이터 {}건이 삭제되었습니다.", deletedCount);
            } else {
                log.info("삭제할 데이터가 없어 삭제하지 않았습니다.");
            }

        } catch (Exception e) {
            log.error("기존 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    public void restoreSnapshot() {
        try {

            // 기존 인덱스를 닫기
            CloseIndexRequest closeIndexRequest = new CloseIndexRequest.Builder()
                    .index(ElasticsearchConstants.INDEX_NAME.toString())  // 닫을 인덱스 이름
                    .build();

            elasticsearchClient.indices().close(closeIndexRequest);
            log.info("인덱스가 닫혔습니다.");

            // 스냅샷 검증
            verifySnapshot(); // 스냅샷 검증 호출

            // 워크어라운드를 사용하여 복원 요청 (8.15 버전 이하에서 snapshot.restore 요청을 보냈을 때, 서버로부터의 응답을 제대로 디코딩할 수 없다는 문제로 발생)
            try (ApiTypeHelper.DisabledChecksHandle h = ApiTypeHelper.DANGEROUS_disableRequiredPropertiesCheck(true)) {
                RestoreResponse restoreResponse = elasticsearchClient.snapshot().restore(r -> r
                        .repository(ElasticsearchConstants.SNAPSHOT_STORAGE_NAME.toString())
                        .snapshot(ElasticsearchConstants.SNAPSHOT_NAME.toString())     // 복원할 스냅샷 이름
                        .indices(ElasticsearchConstants.INDEX_NAME.toString())  // 복원할 인덱스 이름
                );

                if (restoreResponse.snapshot() != null) {
                    log.info("스냅샷 복원이 완료되었습니다. 복원된 인덱스: {}", restoreResponse.snapshot());
                } else {
                    log.warn("스냅샷 복원 후 응답에 snapshot 정보가 없습니다.");
                }
            }

            // 복원 후 인덱스 열기
            OpenRequest openIndexRequest = new OpenRequest.Builder()
                    .index(ElasticsearchConstants.INDEX_NAME.toString())  // 열 인덱스 이름
                    .build();

            elasticsearchClient.indices().open(openIndexRequest);
            log.info("인덱스가 열렸습니다.");

        } catch (Exception e) {
            log.error("스냅샷 복원 중 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void verifySnapshot() {
        try {
            // 스냅샷 상태 확인 (복원할 스냅샷이 정상인지 검증)
            GetSnapshotRequest getSnapshotRequest = new GetSnapshotRequest.Builder()
                    .repository(ElasticsearchConstants.SNAPSHOT_STORAGE_NAME.toString())
                    .snapshot(ElasticsearchConstants.SNAPSHOT_NAME.toString()) // 복원하려는 스냅샷 이름
                    .build();

            GetSnapshotResponse getSnapshotResponse = elasticsearchClient.snapshot().get(getSnapshotRequest);
            if (getSnapshotResponse.snapshots().isEmpty()) {
                log.error("스냅샷이 존재하지 않거나 복원할 수 없습니다.");
            } else {
                log.info("스냅샷의 상태를 확인했습니다. 상태: {}", getSnapshotResponse.snapshots().get(0).state());
            }
        } catch (Exception e) {
            log.error("스냅샷 검증 중 오류 발생: {}", e.getMessage(), e);
        }
    }


    public CreateRepositoryResponse registerS3Repository() {
        try {

            Repository repository = new Repository.Builder()
                    .s3(builder -> builder
                            .settings(settings -> settings
                                    .bucket(S3Constants.BUCKET_NAME.toString())
                                    .basePath(S3Constants.SNAPSHOT_BASE_PATH.toString())
                            ))
                    .build();

            CreateRepositoryRequest repositoryRequest = new CreateRepositoryRequest.Builder()
                    .repository(repository)
                    .name(ElasticsearchConstants.SNAPSHOT_STORAGE_NAME.toString())
                    .build();


            // S3 저장소 등록
            CreateRepositoryResponse response = elasticsearchClient.snapshot().createRepository(repositoryRequest);

            log.info("S3 저장소가 성공적으로 등록되었습니다.");

            return response;
        } catch (Exception e) {
            log.error("S3 저장소 등록에 실패했습니다: {}", e.getMessage(), e);
            throw new RuntimeException("S3 저장소 등록에 실패했습니다.");
        }
    }
}
