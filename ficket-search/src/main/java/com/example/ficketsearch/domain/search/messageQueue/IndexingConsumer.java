package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.dto.IndexingKafkaMessage;
import com.example.ficketsearch.domain.search.enums.IndexingType;
import com.example.ficketsearch.domain.search.service.LockingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingConsumer {

    private final LockingService lockingService;
    private final IndexingService indexingService;

    private static final String INDEXING_LOCK = "Ficket_Indexing_Lock";
    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final long PARTIAL_INDEX_TTL = 5 * 60 * 1000L;   // 5분
    private static final String FULL_INDEXING = IndexingType.FULL_INDEXING.name();

    /**
     * Kafka에서 색인 작업 메시지를 처리하는 메서드
     * 이 메서드는 Kafka 메시지를 받아서, 전체 색인 또는 부분 색인 작업을 처리합니다.
     *
     * @param kafkaMessage - Kafka 메시지 본문 (JSON 형식)
     * @throws RuntimeException - 메시지 처리 중 오류가 발생하면 발생
     */
    @KafkaListener(topics = "indexing-operations")
    public void handleIndexing(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            // Kafka 메시지를 IndexingKafkaMessage 객체로 변환
            IndexingKafkaMessage response = mapper.readValue(kafkaMessage, IndexingKafkaMessage.class);

            String indexingType = response.getIndexingType();

            // 전체 색인 처리
            if (FULL_INDEXING.equals(indexingType)) {
                log.info("전체 색인 작업을 시작합니다. Payload: {}", response.getPayload());
                lockingService.executeWithLock(INDEXING_LOCK, FULL_INDEX_TTL, () -> {
                    indexingService.handleFullIndexing((String) response.getPayload());
                    log.info("전체 색인 작업이 완료되었습니다. Payload: {}", response.getPayload());
                });
                return;
            }

            // 부분 색인 처리
            log.info("부분 색인 작업을 시작합니다. 작업 유형: {}", response.getOperationType());
            lockingService.executeWithLock(INDEXING_LOCK, PARTIAL_INDEX_TTL, () -> {
                String operationType = response.getOperationType();
                switch (operationType) {
                    case "CREATE":
                        log.info("CREATE 작업을 처리 중입니다.");
                        indexingService.handlePartialIndexingCreate((Map<String, Object>) response.getPayload(), operationType);
                        break;

                    case "UPDATE":
                        log.info("UPDATE 작업을 처리 중입니다.");
                        indexingService.handlePartialIndexingUpdate((Map<String, Object>) response.getPayload(), operationType);
                        break;

                    case "DELETE":
                        log.info("DELETE 작업을 처리 중입니다.");
                        indexingService.handlePartialIndexingDelete((String) response.getPayload(), operationType);
                        break;

                    default:
                        log.warn("알 수 없는 작업 유형입니다: {}", operationType);
                        break;
                }
                log.info("부분 색인 작업이 완료되었습니다. 작업 유형: {}", operationType);
            });

        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 처리 중 오류가 발생했습니다. 메시지: {}", kafkaMessage, e);
            throw new RuntimeException("Kafka 메시지 처리 실패", e);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("색인 작업이 중단되었습니다", e);
        }
    }
}
