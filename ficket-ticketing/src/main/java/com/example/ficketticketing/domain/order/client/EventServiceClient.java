package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.*;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.response.TicketDto;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDtoList;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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

    @PostMapping(value = "/api/v1/events/refund", consumes = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Void> refundTicket(@RequestBody TicketInfo ticketInfo);

    @PostMapping(value = "/api/v1/events/available-count", consumes = MediaType.APPLICATION_JSON_VALUE)
    Integer getAvailableCount(@RequestBody TicketDto ticketDto);

    @GetMapping("/api/v1/events/customer-ticket-info")
    TicketSimpleInfo getTicketSimpleInfo(@RequestParam Long ticketId);

    @GetMapping("/api/v1/events/company-id")
    List<Long> getCompanyEventId(@RequestParam Long ticketId);

    @GetMapping("/api/v1/events/events/getScheduleId")
    List<Long> getScheduledId(@RequestParam Long eventId);

    @GetMapping("/api/v1/events/count-ticket/{ticketId}")
    Long getBuyTicketCount(@PathVariable Long ticketId);
}
