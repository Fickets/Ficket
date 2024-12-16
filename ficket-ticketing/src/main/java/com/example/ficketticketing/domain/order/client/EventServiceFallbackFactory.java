package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.ReservedSeatsResponse;
import com.example.ficketticketing.domain.order.dto.client.TicketInfo;
import com.example.ficketticketing.domain.order.dto.client.TicketInfoDto;
import com.example.ficketticketing.domain.order.dto.client.ValidSeatInfoResponse;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.response.TicketDto;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDtoList;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class EventServiceFallbackFactory implements FallbackFactory<EventServiceClient> {

    @Override
    public EventServiceClient create(Throwable cause) {
        return new EventServiceClient() {
            @Override
            public ReservedSeatsResponse getReservedSeats(Long userId, Long eventScheduleId) {
                throw new UnsupportedOperationException("Fallback not implemented for getReservedSeats");
            }

            @Override
            public ValidSeatInfoResponse checkRequest(CreateOrderRequest createOrderRequest) {
                throw new UnsupportedOperationException("Fallback not implemented for checkRequest");
            }

            @Override
            public List<TicketInfoDto> getMyTicketInfo(TicketInfoCreateDtoList ticketInfoCreateDtoList) {
                log.error("Fallback triggered for getMyTicketInfo due to: {}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public LocalDateTime getEventDateTime(Long eventScheduleId) {
                throw new UnsupportedOperationException("Fallback not implemented for checkRequest");
            }

            @Override
            public ResponseEntity<Void> refundTicket(TicketInfo ticketInfo) {
                throw new UnsupportedOperationException("Fallback not implemented for checkRequest");
            }

            @Override
            public Integer getAvailableCount(TicketDto ticketDto) {
                throw new UnsupportedOperationException("Fallback not implemented for checkRequest");
            }
        };
    }
}