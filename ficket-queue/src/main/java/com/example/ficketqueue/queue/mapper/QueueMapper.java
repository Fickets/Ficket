package com.example.ficketqueue.queue.mapper;

import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.enums.QueueStatus;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface QueueMapper {
    MyQueueStatusResponse toMyQueueStatusResponse(String userId, String eventId, Long myWaitingNumber, Long totalWaitingNumber, QueueStatus queueStatus);
}
