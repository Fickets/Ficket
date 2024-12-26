package com.example.ficketticketing.domain.order.mapper;

import com.example.ficketticketing.domain.order.dto.client.OrderSimpleDto;
import com.example.ficketticketing.domain.order.entity.Orders;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    OrderSimpleDto toOrderSimpleDto(Orders orders);

}
