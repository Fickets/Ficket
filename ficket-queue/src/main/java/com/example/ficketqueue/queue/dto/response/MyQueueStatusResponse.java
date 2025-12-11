package com.example.ficketqueue.queue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyQueueStatusResponse {
    private String userId;
    private String eventId;
    private Long myWaitingNumber;
    private Long totalWaitingNumber;
    private QueueStatus queueStatus;
}
