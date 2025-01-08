package com.example.ficketticketing.domain.order.messagequeue;

import com.example.ficketticketing.domain.order.dto.kafka.SeatMappingUpdatedEvent;
import com.example.ficketticketing.domain.order.entity.OrderStatus;
import com.example.ficketticketing.domain.order.repository.OrderRepository;
import com.example.ficketticketing.domain.order.service.OrderService;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import com.example.ficketticketing.infrastructure.payment.PortOneApiClient;
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
    private final OrderRepository orderRepository;
    private final PortOneApiClient portOneApiClient;

    @Transactional
    @KafkaListener(topics = "seat-mapping-events", groupId = "order-group")
    public void consumeSeatMappingUpdatedEvent(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            SeatMappingUpdatedEvent event = mapper.readValue(message, SeatMappingUpdatedEvent.class);
            log.info("SeatMappingUpdated event received: {}", event);

            if (event.isSuccess()) {
                orderRepository.updateOrderStatus(event.getOrderId(), OrderStatus.COMPLETED);
            } else {
                portOneApiClient.cancelOrder(String.valueOf(event.getOrderId()));
                orderRepository.updateOrderStatus(event.getOrderId(), OrderStatus.CANCELLED);
            }
        }
        catch (BusinessException e) {
            log.error("Failed to process SeatMappingUpdated event", e);
        }
        catch (JsonProcessingException e) {
            log.error("Failed to process SeatMappingUpdated event", e);
            throw new BusinessException(ErrorCode.Json_Processing_Exception);
        }
    }
}