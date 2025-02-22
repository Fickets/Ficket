package com.example.ficketevent.domain.event.messagequeue;

import com.example.ficketevent.domain.event.dto.kafka.OrderDto;
import com.example.ficketevent.domain.event.service.StageSeatService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Slf4j
@RequiredArgsConstructor
public class OrderConsumer {

    private final StageSeatService stageSeatService;

    @Transactional
    @KafkaListener(topics = "order-events")
    public void createOrder(String kafkaMessage) {

        log.info("Kafka Message: -> " + kafkaMessage);

        ObjectMapper mapper = new ObjectMapper();
        try {
            OrderDto orderEvent = mapper.readValue(kafkaMessage, OrderDto.class);
            stageSeatService.handleOrderCreatedEvent(orderEvent);
        } catch (JsonProcessingException e) {
            log.error("Failed to process OrderCreated event", e);
        }
    }

}