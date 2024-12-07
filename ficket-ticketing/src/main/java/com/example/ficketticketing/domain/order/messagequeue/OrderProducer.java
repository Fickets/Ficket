package com.example.ficketticketing.domain.order.messagequeue;

import com.example.ficketticketing.domain.order.dto.kafka.OrderDto;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class OrderProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;

    public void send(String topic, OrderDto orderDto) {
        ObjectMapper mapper = new ObjectMapper();
        String jsonInString = "";
        try {
            jsonInString = mapper.writeValueAsString(orderDto);
        } catch (JsonProcessingException e) {
            log.error("메세지 전송 실패 : {}", jsonInString);
            throw new BusinessException(ErrorCode.Json_Processing_Exception);
        }

        kafkaTemplate.send(topic, jsonInString);

        log.info("Kafka Producer send data " + orderDto);
    }
}