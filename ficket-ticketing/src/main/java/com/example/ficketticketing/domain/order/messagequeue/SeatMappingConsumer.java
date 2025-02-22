package com.example.ficketticketing.domain.order.messagequeue;

import com.example.ficketticketing.domain.order.dto.kafka.SeatMappingUpdatedEvent;
import com.example.ficketticketing.domain.order.service.OrderService;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SeatMappingConsumer {

    private final OrderService orderService;

    @Transactional
    @KafkaListener(topics = "seat-mapping-events", groupId = "order-group")
    public void consumeSeatMappingUpdatedEvent(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            SeatMappingUpdatedEvent event = mapper.readValue(message, SeatMappingUpdatedEvent.class);
            log.info("SeatMappingUpdated event received: {}", event);

            if (!event.isSuccess()) {
                orderService.cancelOrder(event.getOrderId());
            }

        } catch (BusinessException e) {
            log.error("Failed to process SeatMappingUpdated event", e);
        } catch (JsonProcessingException e) {
            log.error("Failed to process SeatMappingUpdated event", e);
            throw new BusinessException(ErrorCode.Json_Processing_Exception);
        }
    }
}