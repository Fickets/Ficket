package com.example.ficketticketing.domain.order.mapper;

import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.request.UploadFaceInfo;
import com.example.ficketticketing.domain.order.dto.response.TicketDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TicketMapper {

    TicketDto toTicketDto(Long eventScheduleId, List<Long> ticketIds);

    @Mapping(source = "createOrderRequest.faceId", target = "faceId")
    @Mapping(source = "createOrderRequest.faceImgUrl", target = "faceImgUrl")
    @Mapping(source = "ticketId", target = "ticketId")
    @Mapping(source = "createOrderRequest.eventScheduleId", target = "eventScheduleId")
    UploadFaceInfo toUploadFaceInfo(CreateOrderRequest createOrderRequest, Long ticketId);
}
