package com.example.ficketuser.client;

import com.example.ficketuser.dto.client.TicketInfoDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ticketing-service", fallbackFactory = TicketingServiceClientFallbackFactory.class)
public interface TicketingServiceClient {

    @GetMapping("api/v1/ticketing/order/my")
    List<TicketInfoDto> getMyTickets(@RequestParam Long userId);

}
