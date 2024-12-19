package com.example.ficketuser.client;

import com.example.ficketuser.dto.client.OrderInfoDto;
import com.example.ficketuser.dto.client.TicketInfoDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class TicketingServiceClientFallbackFactory implements FallbackFactory<TicketingServiceClient> {

    @Override
    public TicketingServiceClient create(Throwable cause) {
        return new TicketingServiceClient() {
            @Override
            public List<TicketInfoDto> getMyTickets(Long userId) {
                log.error("Fallback triggered due to: {}", cause.getMessage());
                return Collections.emptyList();
            }

            @Override
            public List<OrderInfoDto> getCustomerTicket(Long userId) {
                return Collections.emptyList();
            }
        };
    }
}