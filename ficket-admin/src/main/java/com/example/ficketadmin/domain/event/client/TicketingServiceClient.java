package com.example.ficketadmin.domain.event.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "ticketing-service")
public interface TicketingServiceClient {

    @GetMapping("/api/v1/ticketing/check/ticket-watch/{ticketId}")
    ResponseEntity<Void> ticketWatchedChange(@PathVariable(name = "ticketId")Long ticketId,
                                             @RequestParam(name = "eventId")Long eventId,
                                             @RequestParam(name = "connectId")Long connectId);
}
