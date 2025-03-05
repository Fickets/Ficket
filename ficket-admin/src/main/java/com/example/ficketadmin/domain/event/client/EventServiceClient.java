package com.example.ficketadmin.domain.event.client;

import com.example.ficketadmin.domain.check.dto.TicketSimpleInfo;
import com.example.ficketadmin.domain.event.dto.response.DailyRevenueResponse;
import com.example.ficketadmin.domain.event.dto.response.DayCountResponse;
import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "event-service")
public interface EventServiceClient {

    @GetMapping("/api/v1/events/{eventId}/daily-revenue")
    List<DailyRevenueResponse> calculateDailyRevenue(@PathVariable Long eventId);

    @GetMapping("/api/v1/events/{eventId}/day-count")
    DayCountResponse calculateDayCount(@PathVariable Long eventId);

    @GetMapping("/api/v1/events/search-ids")
    List<EventTitleDto> getEventIds(@RequestParam String title);

    @GetMapping("/api/v1/events/events/getScheduleId")
    List<Long> getScheduledId(@RequestParam Long eventId);

    @GetMapping("/api/v1/events/customer-ticket-info")
    TicketSimpleInfo getTicketSimpleInfo(@RequestParam Long ticketId);

    @GetMapping("/api/v1/events/count-ticket/{ticketId}")
    Long getBuyTicketCount(@PathVariable Long ticketId);

    @GetMapping("/api/v1/events/companyId/{eventId}")
    Long getCompanyByEvent(@PathVariable Long eventId);
}
