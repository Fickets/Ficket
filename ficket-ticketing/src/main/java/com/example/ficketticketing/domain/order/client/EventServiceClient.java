package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.ReservedSeatsResponse;
import com.example.ficketticketing.domain.order.dto.client.TicketInfoDto;
import com.example.ficketticketing.domain.order.dto.client.ValidSeatInfoResponse;
import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

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


    @GetMapping("/api/v1/events/my-ticket-info")
    List<TicketInfoDto> getMyTicketInfo(@RequestParam List<Long> ticketIds);
}
