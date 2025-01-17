package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.TicketingServiceClient;
import com.example.ficketevent.domain.event.dto.common.DailyRevenueResponse;
import com.example.ficketevent.domain.event.dto.common.DayCountResponse;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.CircuitBreakerUtils;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Slf4j
@Service
@AllArgsConstructor
public class EventStatisticService {

    private final EventRepository eventRepository;
    private final TicketingServiceClient ticketingServiceClient;
    private final CircuitBreakerRegistry circuitBreakerRegistry;

    public int[] getGenderDistributionChart(Long eventId){

        List<Long> eventSchedules = eventRepository.getEventScheduleByEventId(eventId);
        int[] userStatistic = ticketingServiceClient.getTicketUserStatistic(eventSchedules);

        return userStatistic;
    }

    public List<DailyRevenueResponse> calculateDailyRevenue(Long eventId){

        Set<Long> ticketIds = eventRepository.getTicketIdsByEventId(eventId);

        log.info("날짜별 수익 - 티켓 리스트 ticketIds: {}", ticketIds);

        return CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "calculateDailyRevenueCircuitBreaker",
                () -> ticketingServiceClient.calculateDailyRevenue(ticketIds)
        );
    }


    public DayCountResponse calculateDayCount(Long eventId){
        Set<Long> ticketIds = eventRepository.getTicketIdsByEventId(eventId);

        log.info("요일별 예매수 - 티켓 리스트 : ticketIds: {}", ticketIds);

        return CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "calculateDayCountCircuitBreaker",
                () -> ticketingServiceClient.calculateDayCount(ticketIds)
        );
    }

}
