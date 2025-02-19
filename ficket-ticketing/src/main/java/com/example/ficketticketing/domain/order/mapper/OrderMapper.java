package com.example.ficketticketing.domain.order.mapper;

import com.example.ficketticketing.domain.order.dto.client.OrderSimpleDto;
import com.example.ficketticketing.domain.order.entity.Orders;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(source = "orders.ticket.ticketId", target = "ticketId")
    OrderSimpleDto toOrderSimpleDto(Orders orders);

}
