package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.UserSimpleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/v1/users/{userId}")
    UserSimpleDto getUser(@PathVariable Long userId);
}
