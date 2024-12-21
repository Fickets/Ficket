package com.example.ficketticketing.domain.order.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "queue-service")
public interface QueueServiceClient {

    @PostMapping("/api/v1/queues/{userId}/send-order-status")
    Void sendOrderStatus(@PathVariable Long userId, @RequestParam("orderStatus") String orderStatus);
}
