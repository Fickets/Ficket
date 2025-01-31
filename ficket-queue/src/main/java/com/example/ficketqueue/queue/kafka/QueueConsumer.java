package com.example.ficketqueue.queue.kafka;

import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.queue.dto.response.QueueMessage;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueConsumer {

    @Qualifier("queueReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> queueReactiveRedisTemplate;
    private final ObjectMapper objectMapper;

    @KafkaListener(topicPattern = "ficket-queue-.*", groupId = "ticketing-group", concurrency = "3")
    public void consume(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String eventId = topic.replace("ficket-queue-", "");
        String messageValue = record.value();

        try {
            // Kafka 메시지를 QueueMessage 객체로 변환
            QueueMessage queueMessage = objectMapper.readValue(messageValue, QueueMessage.class);

            log.info("Kafka 메시지 수신 [Event: {}, User: {}, CurrentTime: {}]",
                    eventId, queueMessage.getUserId(), queueMessage.getCurrentTime());

            String redisKey = KeyHelper.getFicketRedisQueue(eventId);

            // Redis ZSet에 currentTime을 score로 추가
            addToRedisZSet(redisKey, queueMessage.getUserId(), eventId, queueMessage.getCurrentTime());

        } catch (Exception e) {
            log.error("Kafka 메시지 처리 실패 [Topic: {}, Message: {}, Error: {}]", topic, messageValue, e.getMessage());
        }
    }

    private void addToRedisZSet(String redisKey, String userId, String eventId, double score) {
        queueReactiveRedisTemplate.opsForZSet()
                .add(redisKey, userId, score) // 중복된 userId는 자동으로 무시됨
                .doOnSuccess(added -> {
                    if (Boolean.TRUE.equals(added)) {
                        log.info("Redis ZSet에 추가됨 [Event: {}, User: {}, Score: {}]", eventId, userId, score);
                    } else {
                        log.warn("이미 존재하는 사용자 [Event: {}, User: {}]", eventId, userId);
                    }
                })
                .doOnError(ex -> log.error("Redis ZSet 추가 실패 [Event: {}, User: {}, Error: {}]", eventId, userId, ex.getMessage()))
                .subscribe();
    }
}
