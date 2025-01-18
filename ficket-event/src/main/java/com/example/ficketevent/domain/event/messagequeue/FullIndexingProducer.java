package com.example.ficketevent.domain.event.messagequeue;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class FullIndexingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    private static final String TOPIC_NAME = "full-indexing";

    public void sendIndexingMessage(String message) {
        kafkaTemplate.send(TOPIC_NAME, message);
        log.info("Kafka로 메시지를 전송했습니다: {}", message);
    }
}
