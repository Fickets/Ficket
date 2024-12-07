package com.example.ficketevent.domain.event.messagequeue;

import com.example.ficketevent.domain.event.dto.kafka.SeatMappingUpdatedEvent;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatMappingProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void publishSeatMappingUpdatedEvent(SeatMappingUpdatedEvent event) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            String message = mapper.writeValueAsString(event);
            kafkaTemplate.send("seat-mapping-events", message);
            log.info("Published SeatMappingUpdatedEvent: {}", message);
        } catch (JsonProcessingException e) {
            log.error("Failed to publish SeatMappingUpdatedEvent", e);
        }
    }
}
