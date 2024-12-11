package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.ReservedSeatsResponse;
import com.example.ficketticketing.domain.order.dto.client.TicketInfoDto;
import com.example.ficketticketing.domain.order.dto.client.ValidSeatInfoResponse;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.response.TicketDto;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDtoList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@FeignClient(name = "event-service", fallbackFactory = EventServiceFallbackFactory.class)
public interface EventServiceClient {

    @GetMapping("/api/v1/events")
    ReservedSeatsResponse getReservedSeats(
            @RequestParam("userId") Long userId,
            @RequestParam("eventScheduleId") Long eventScheduleId
    );

    @PostMapping(value = "/api/v1/events/valid-request", consumes = MediaType.APPLICATION_JSON_VALUE)
    ValidSeatInfoResponse checkRequest(@RequestBody CreateOrderRequest createOrderRequest);


    @PostMapping(value = "/api/v1/events/my-ticket-info", consumes = MediaType.APPLICATION_JSON_VALUE)
    List<TicketInfoDto> getMyTicketInfo(@RequestBody TicketInfoCreateDtoList ticketInfoCreateDtoList);

    @GetMapping("/api/v1/events/date/time/{eventScheduleId}")
    LocalDateTime getEventDateTime(@PathVariable Long eventScheduleId);

    @PostMapping("/api/v1/events/refund/{ticketId}")
    ResponseEntity<Void> refundTicket(@PathVariable Long ticketId);

    @PostMapping(value = "/api/v1/events/available-count", consumes = MediaType.APPLICATION_JSON_VALUE)
    Integer getAvailableCount(@RequestBody TicketDto ticketDto);
}
