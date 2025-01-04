package com.example.ficketevent.domain.event.messagequeue;

import com.example.ficketevent.domain.event.enums.IndexingType;
import com.example.ficketevent.domain.event.enums.OperationType;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOPIC_NAME = "indexing-operations";

    /**
     * 색인 작업 메시지를 Kafka로 전송합니다.
     *
     * @param indexingType  색인 타입 (전체 색인, 부분 색인)
     * @param payload       색인 데이터 (S3 URL 또는 이벤트 정보 또는 삭제 eventId)
     * @param operationType 동작 타입 (생성, 수정, 삭제)
     */
    public void sendIndexingMessage(IndexingType indexingType, Object payload, OperationType operationType) {
        try {
            String message = objectMapper.writeValueAsString(
                    new IndexingMessage(indexingType, payload, operationType)
            );

            kafkaTemplate.send(TOPIC_NAME, message);
            log.info("Kafka로 메시지를 전송했습니다: {}", message);
        } catch (JsonProcessingException e) {
            log.error("색인 메시지 직렬화 실패. 타입: {}, 데이터: {}, 오류: {}",
                    indexingType, payload, e.getMessage());
            throw new RuntimeException("Kafka로 메시지 전송 실패", e);
        }
    }

    @Getter
    private static class IndexingMessage {
        private final String indexingType; // FULL_INDEXING or PARTIAL_INDEXING
        private final Object payload;
        private final String operationType; // "CREATE", "UPDATE", "DELETE"

        public IndexingMessage(IndexingType indexingType, Object payload, OperationType operationType) {
            this.indexingType = indexingType.name();
            this.payload = payload;
            this.operationType = operationType.name();
        }
    }
}
