package com.example.ficketadmin.domain.event.client;


import com.example.ficketadmin.domain.check.dto.UserSimpleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticketing-service")
public interface TicketingServiceClient {

    @GetMapping("/api/v1/ticketing/order/getUserId")
    UserSimpleDto getUserIdByTicketId(@RequestParam(name = "ticketId") Long ticketId);

    @GetMapping("api/v1/ticketing/order/ticket-watch")
    ResponseEntity<Void> changeTicketWatched(@RequestParam(name = "ticketId") Long ticketId);
}
