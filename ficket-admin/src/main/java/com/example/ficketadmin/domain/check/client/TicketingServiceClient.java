package com.example.ficketadmin.domain.check.client;

import com.example.ficketadmin.domain.check.dto.UserSimpleDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

public interface TicketingServiceClient {

    @GetMapping("/api/v1/ticketing/order/getUserId")
    UserSimpleDto getUserIdByTicketId(@RequestParam(name = "ticketId")Long ticketId);

    @GetMapping("api/v1/ticketing/order/ticket-watch")
    ResponseEntity<Void> changeTicketWatched(@RequestParam(name = "ticketId")Long ticketId);


}
