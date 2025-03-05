package com.example.ficketqueue.queue.kafka;

import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.queue.dto.response.QueueMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void addQueue(String userId, String eventId) {
        String topic = KeyHelper.getFicketKafkaQueue(eventId);
        sendMessage(topic, userId);
    }

    private void sendMessage(String topic, String userId) {
        long currentTime = Instant.now().toEpochMilli();
        QueueMessage queueMessage = new QueueMessage(userId, currentTime);

        try {
            // 메시지를 JSON 문자열로 변환
            String messageJson = objectMapper.writeValueAsString(queueMessage);

            // Kafka 메시지 전송 (키 없이 전송)
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, messageJson);

            future.whenComplete((result, ex) -> {
                if (ex == null) {
                    // 메시지 전송 성공
                    RecordMetadata metadata = result.getRecordMetadata();
                    log.info("메시지 전송 성공 [User: {}, CurrentTime: {}, Partition: {}, Offset: {}]",
                            userId, currentTime, metadata.partition(), metadata.offset());
                } else {
                    // 메시지 전송 실패
                    log.error("메시지 전송 실패 [User: {}, CurrentTime: {}, Error: {}]", userId, currentTime, ex.getMessage());
                }
            });

        } catch (JsonProcessingException e) {
            log.error("메시지 변환 실패 [User: {}, Error: {}]", userId, e.getMessage());
        }
    }
}
