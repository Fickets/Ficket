package com.example.ficketuser.domain.client;

import com.example.ficketuser.domain.dto.client.OrderInfoDto;
import com.example.ficketuser.domain.dto.client.TicketInfoDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "ticketing-service", fallbackFactory = TicketingServiceClientFallbackFactory.class)
public interface TicketingServiceClient {

    @GetMapping("/api/v1/ticketing/order/my")
    List<TicketInfoDto> getMyTickets(@RequestParam Long userId);

    @GetMapping("/api/v1/ticketing/order/customer")
    List<OrderInfoDto> getCustomerTicket(@RequestParam Long userId);
}
