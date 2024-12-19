package com.example.ficketqueue.scheduler;

import com.example.ficketqueue.service.QueueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TicketingOpenScheduler {

    private QueueService queueService;

    @Scheduled(cron = "0 0 3 * * ?")
    public void ticketOpen() {
        queueService.openTicketing();
    }

}
