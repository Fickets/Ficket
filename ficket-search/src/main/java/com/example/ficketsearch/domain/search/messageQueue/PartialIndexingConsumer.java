package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.dto.*;
import com.example.ficketsearch.domain.search.service.IndexingLockService;
import com.example.ficketsearch.domain.search.service.KafkaControlService;
import com.example.ficketsearch.domain.search.service.PartialIndexingService;
import com.example.ficketsearch.global.config.kafka.KafkaConstants;
import com.example.ficketsearch.global.config.redis.IndexingLockConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PartialIndexingConsumer {

    private final IndexingLockService indexingLockService;
    private final PartialIndexingService partialIndexingService;
    private final KafkaControlService kafkaControlService;
    private final ObjectMapper objectMapper;


    @KafkaListener(
            topics = "#{T(com.example.ficketsearch.global.config.kafka.KafkaConstants).INDEXING_CONTROL_TOPIC.value}",
            groupId = "#{T(com.example.ficketsearch.global.config.kafka.KafkaConstants).INDEXING_CONTROL_GROUP.value}"
    )
    public void handleIndexingControl(String message) {
        if (KafkaConstants.FULL_INDEXING_FINISHED.toString().equals(message)) {
            kafkaControlService.resumePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
            log.info("전체 색인 완료 이벤트 수신 → Kafka Consumer resume");
        }
    }

    @KafkaListener(
            id = "#{T(com.example.ficketsearch.global.config.kafka.KafkaConstants).PARTIAL_INDEXING_LISTENER.value}",
            topics = "#{T(com.example.ficketsearch.global.config.kafka.KafkaConstants).PARTIAL_INDEXING_TOPIC.value}",
            groupId = "#{T(com.example.ficketsearch.global.config.kafka.KafkaConstants).PARTIAL_INDEXING_GROUP.value}"
    )
    public void handlePartialIndexing(String message) {

        // 1. Read Lock 획득 시도
        boolean lockAcquired = indexingLockService.acquirePartialIndexingLock(
                IndexingLockConstants.PARTIAL_INDEXING_WAIT_TIME_SEC.toLong(),
                IndexingLockConstants.PARTIAL_INDEXING_LEASE_TIME_SEC.toLong()
        );


        if (!lockAcquired) {
            log.warn("부분 색인 Read Lock 획득 실패 (전체 색인 진행 중) → Kafka pause");
            kafkaControlService.pausePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());

            // 메시지를 처리하지 않고 예외를 던져 Kafka가 재시도하도록 함
            throw new RuntimeException("전체 색인 진행 중이므로 부분 색인 불가");
        }

        try {
            // 2. 메시지 파싱 및 처리
            PartialIndexingMessage<?> msg = parseMessage(message);
            processMessage(msg);

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 실패: {}", message, e);
            throw new RuntimeException("Kafka 메시지 처리 실패", e);

        } finally {
            // 3. Read Lock 해제 (반드시 실행)
            indexingLockService.releasePartialIndexingLock();
        }
    }

    private PartialIndexingMessage<?> parseMessage(String message) throws JsonProcessingException {
        PartialIndexingMessage<?> msg = objectMapper.readValue(message, PartialIndexingMessage.class);
        log.info("부분 색인 Kafka 메시지 수신: {}", msg);
        return msg;
    }

    private void processMessage(PartialIndexingMessage<?> msg) {
        switch (msg.getOperationType()) {
            case "CREATE" -> processCreate(msg);
            case "UPDATE" -> processUpdate(msg);
            case "DELETE" -> processDelete(msg);
            default -> log.warn("알 수 없는 작업 유형: {}", msg.getOperationType());
        }
    }

    private void processCreate(PartialIndexingMessage<?> msg) {
        PartialIndexingUpsertDto dto = objectMapper.convertValue(msg.getPayload(), PartialIndexingUpsertDto.class);
        log.info("CREATE 작업 처리: {}", dto);
        partialIndexingService.upsertDocument(dto);
    }

    private void processUpdate(PartialIndexingMessage<?> msg) {
        PartialIndexingUpsertDto dto = objectMapper.convertValue(msg.getPayload(), PartialIndexingUpsertDto.class);
        log.info("UPDATE 작업 처리: {}", dto);
        partialIndexingService.upsertDocument(dto);
    }

    private void processDelete(PartialIndexingMessage<?> msg) {
        PartialIndexingDeleteDto dto = objectMapper.convertValue(msg.getPayload(), PartialIndexingDeleteDto.class);
        log.info("DELETE 작업 처리: {}", dto);
        partialIndexingService.deleteDocument(dto);
    }
}