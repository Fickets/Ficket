package com.example.ficketqueue.queue.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/v1/events/today-open-events")
    List<Long> getTodayOpenEvents();

    @GetMapping("/api/v1/events/yesterday-open-events")
    List<Long> getYesterdayOpenEvents();
}
