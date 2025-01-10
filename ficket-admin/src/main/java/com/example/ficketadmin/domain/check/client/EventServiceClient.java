package com.example.ficketadmin.domain.check.client;

import com.example.ficketadmin.domain.check.dto.TicketSimpleInfo;
import com.example.ficketadmin.domain.event.client.EventServiceClientFallbackFactory;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "event-service", fallback = EventServiceClientFallbackFactory.class)
public interface EventServiceClient {

    @GetMapping("/api/v1/events/events/getScheduleId")
    List<Long> getScheduledId(@RequestParam Long eventId);

    @GetMapping("/api/v1/events/customer-ticket-info")
    TicketSimpleInfo getTicketSimpleInfo(@RequestParam Long ticketId);

}
