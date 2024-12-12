package com.example.ficketadmin.domain.event.client;

import com.example.ficketadmin.domain.event.dto.response.DailyRevenueResponse;
import com.example.ficketadmin.domain.event.dto.response.DayCountResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "event-service", fallback = EventServiceClientFallbackFactory.class)
public interface EventServiceClient {

    @GetMapping("/api/v1/events/{eventId}/daily-revenue")
    List<DailyRevenueResponse> calculateDailyRevenue(@PathVariable Long eventId);

    @GetMapping("/api/v1/events/{eventId}/day-count")
    DayCountResponse calculateDayCount(@PathVariable Long eventId);
}
