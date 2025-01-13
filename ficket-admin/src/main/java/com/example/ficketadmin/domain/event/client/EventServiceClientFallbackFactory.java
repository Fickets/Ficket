package com.example.ficketadmin.domain.event.client;


import com.example.ficketadmin.domain.check.dto.TicketSimpleInfo;
import com.example.ficketadmin.domain.event.dto.response.DailyRevenueResponse;
import com.example.ficketadmin.domain.event.dto.response.DayCountResponse;
import com.example.ficketadmin.domain.settlement.dto.common.EventTitleDto;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class EventServiceClientFallbackFactory implements FallbackFactory<EventServiceClient> {

    @Override
    public EventServiceClient create(Throwable cause) {
        return new EventServiceClient() {

            @Override
            public List<DailyRevenueResponse> calculateDailyRevenue(Long eventId) {
                return Collections.emptyList();
            }

            @Override
            public DayCountResponse calculateDayCount(Long eventId) {
                Map<String, Long> dayCountMap = new LinkedHashMap<>();
                dayCountMap.put("Monday", 0L);
                dayCountMap.put("Tuesday", 0L);
                dayCountMap.put("Wednesday", 0L);
                dayCountMap.put("Thursday", 0L);
                dayCountMap.put("Friday", 0L);
                dayCountMap.put("Saturday", 0L);
                dayCountMap.put("Sunday", 0L);

                return new DayCountResponse(dayCountMap);
            }

            @Override
            public List<EventTitleDto> getEventIds(String title) {
                return List.of();
            }

            @Override
            public List<Long> getScheduledId(Long eventId) {
                throw new UnsupportedOperationException("Fallback not implemented for getReservedSeats");
            }

            @Override
            public TicketSimpleInfo getTicketSimpleInfo(Long ticketId) {
                throw new UnsupportedOperationException("Fallback not implemented for getReservedSeats");
            }
        };
    }
}
