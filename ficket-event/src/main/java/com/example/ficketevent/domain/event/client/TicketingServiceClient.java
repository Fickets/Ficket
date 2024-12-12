package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.DailyRevenueResponse;
import com.example.ficketevent.domain.event.dto.common.DayCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@FeignClient(name = "ticketing-service")
public interface TicketingServiceClient {

    @PostMapping("/api/v1/ticketing/order/all-user-id")
    int[] getTicketUserStatistic(List<Long> eventSchedules);

    @GetMapping("/api/v1/ticketing/order/enter-ticketing/{eventScheduleId}")
    Integer enterTicketing(@RequestHeader("X-User-Id") String userId, @PathVariable Long eventScheduleId);

    @GetMapping("/api/v1/ticketing/order/daily-revenue")
    List<DailyRevenueResponse> calculateDailyRevenue(@RequestParam("ticketIds") Set<Long> ticketIds);

    @GetMapping("/api/v1/ticketing/order/day-count")
    DayCountResponse calculateDayCount(@RequestParam("ticketIds") Set<Long> ticketIds);
}
