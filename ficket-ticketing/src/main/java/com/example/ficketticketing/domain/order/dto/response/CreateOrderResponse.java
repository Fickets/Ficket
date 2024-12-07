package com.example.ficketticketing.domain.order.dto.response;

import com.example.ficketticketing.domain.order.entity.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateOrderResponse {
    private OrderStatus orderStatus;
}
