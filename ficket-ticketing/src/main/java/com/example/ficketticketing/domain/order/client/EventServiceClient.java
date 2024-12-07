package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.ReservedSeatsResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/v1/events")
    ReservedSeatsResponse getReservedSeats(
            @RequestParam("userId") Long userId,
            @RequestParam("eventScheduleId") Long eventScheduleId
    );
}
