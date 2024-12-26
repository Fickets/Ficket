package com.example.ficketticketing.domain.order.client;

import com.example.ficketticketing.domain.order.dto.client.OrderSimpleDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.servlet.function.EntityResponse;

@FeignClient(name = "admins-service")
public interface AdminServiceClient {

    @PostMapping("api/v1/settlements/create")
    EntityResponse<Void> createSettlement(@RequestBody OrderSimpleDto orderSimpleDto);


}
