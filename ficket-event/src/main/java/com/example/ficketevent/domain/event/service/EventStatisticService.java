package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.TicketingServiceClient;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import com.example.ficketevent.domain.event.repository.EventCustomRepository;
import com.example.ficketevent.domain.event.repository.EventCustomRepositoryImpl;
import com.example.ficketevent.domain.event.repository.EventRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@AllArgsConstructor
public class EventStatisticService {

    private final EventRepository eventRepository;
    private final TicketingServiceClient ticketingServiceClient;

    public int[] getGenderDistributionChart(Long eventId){

        List<Long> eventSchedules = eventRepository.getEventScheduleByEventId(eventId);
        int[] userStatistic = ticketingServiceClient.getTicketUserStatistic(eventSchedules);

        return userStatistic;
    }
}
