package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.OrderStatisticDto;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@FeignClient(name = "ticketing-service")
public interface TicketingServiceClient {

    @PostMapping("/api/v1/ticketing/order/all-user-id")
    int[] getTicketUserStatistic(List<Long> eventSchedules);


}
