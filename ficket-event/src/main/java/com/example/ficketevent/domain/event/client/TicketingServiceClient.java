package com.example.ficketevent.domain.event.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "ticketing-service")
public interface TicketingServiceClient {

    @PostMapping("/api/v1/ticketing/order/all-user-id")
    int[] getTicketUserStatistic(List<Long> eventSchedules);

    @GetMapping("/api/v1/ticketing/order/enter-ticketing/{eventScheduleId}")
    Integer enterTicketing(@RequestHeader("X-User-Id") String userId, @PathVariable Long eventScheduleId);
}
