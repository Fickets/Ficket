package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.UserSimpleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    UserSimpleDto getUser(@PathVariable Long userId);
}
