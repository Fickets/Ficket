package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.OrderSimpleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;

@FeignClient(name = "admin-service")
public interface AdminServiceClient {

    @PostMapping("api/v1/settlements/create")
    ResponseEntity<Void> createSettlement(@RequestBody OrderSimpleDto orderSimpleDto);

    @GetMapping("api/v1/settlements/refund")
    ResponseEntity<Void> refundSettlement(@RequestParam(name = "orderId") Long orderId, @RequestParam(name = "ticketId") Long ticketId, @RequestParam(name = "refund") BigDecimal refund);
}
