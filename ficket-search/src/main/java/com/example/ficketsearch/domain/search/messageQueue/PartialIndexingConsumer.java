package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.dto.*;
import com.example.ficketsearch.domain.search.service.LockingService;
import com.example.ficketsearch.domain.search.service.PartialIndexingService;
import com.example.ficketsearch.domain.search.service.RedisQueueService;
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

    private final LockingService lockingService;
    private final PartialIndexingService partialIndexingService;
    private final RedisQueueService redisQueueService;
    private final ObjectMapper objectMapper;

    @KafkaListener(
            id = "partialIndexingListener",
            topics = "partial-indexing",
            groupId = "partial-indexing-group"
    )
    public void handlePartialIndexing(String message) {
        try {
            PartialIndexingMessage<?> partialIndexingMessage = parseMessage(message);

            if (lockingService.isLocked()) {
                log.info("전체 색인 중 → Redis 대기 큐에 저장: {}", message);
                redisQueueService.enqueue(message);
                return;
            }

            processMessage(partialIndexingMessage);

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 실패: {}", message, e);
            throw new RuntimeException("Kafka 메시지 처리 실패", e);
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
