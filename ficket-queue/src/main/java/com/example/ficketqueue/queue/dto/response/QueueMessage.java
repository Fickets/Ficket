package com.example.ficketqueue.queue.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QueueMessage {
    private String userId;
    private long currentTime;
}