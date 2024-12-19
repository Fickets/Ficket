package com.example.ficketqueue.mapper;

import com.example.ficketqueue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.enums.QueueStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QueueMapper {
    MyQueueStatusResponse toMyQueueStatusResponse(String userId, String eventId, Long myWaitingNumber,Long totalWaitingNumber, QueueStatus queueStatus);
}
