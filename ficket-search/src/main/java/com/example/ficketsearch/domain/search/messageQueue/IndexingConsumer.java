package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.dto.IndexingKafkaMessage;
import com.example.ficketsearch.domain.search.enums.IndexingType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingConsumer {

    private final IndexingService indexingService;

    @KafkaListener(topics = "indexing-operations")
    public void handleIndexing(String kafkaMessage) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            IndexingKafkaMessage response = mapper.readValue(kafkaMessage, IndexingKafkaMessage.class);
            if (response.getIndexingType().equals(IndexingType.FULL_INDEXING.name())) {
                indexingService.handleFullIndexing((String) response.getPayload());
                return;
            }

            // 부분 색인 작업 처리
            String operationType = response.getOperationType();
            switch (operationType) {
                case "CREATE":
                    log.info("Processing CREATE operation");
                    indexingService.handlePartialIndexingCreate((Map<String, Object>) response.getPayload());
                    break;

                case "UPDATE":
                    log.info("Processing UPDATE operation");
                    indexingService.handlePartialIndexingUpdate((Map<String, Object>) response.getPayload());
                    break;

                case "DELETE":
                    log.info("Processing DELETE operation");
                    indexingService.handlePartialIndexingDelete((String) response.getPayload());
                    break;

                default:
                    log.warn("Unknown operation type: {}", operationType);
                    break;
            }

        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
