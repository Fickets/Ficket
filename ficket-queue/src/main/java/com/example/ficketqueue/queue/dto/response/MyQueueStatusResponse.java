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
    private Long myWaitingNumber;
    private Long totalWaitingNumber;
    private Boolean canEnter;

    public static MyQueueStatusResponse of(Long myWaitingNumber, Long totalWaitingNumber, Boolean canEnter) {
        return MyQueueStatusResponse.builder()
                .myWaitingNumber(myWaitingNumber)
                .totalWaitingNumber(totalWaitingNumber)
                .canEnter(canEnter)
                .build();
    }
}
