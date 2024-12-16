package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.dto.common.EventScheduleDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReservationRateEventInfoResponse {
    private Long eventId;
    private String eventTitle;
    private String eventMobilePosterUrl;
    private String eventPcPosterUrl;
    private String eventStageName;
    private Set<LocalDate> eventDates;
    private Boolean isClosed;
    private BigDecimal reservationRate;


    public static ReservationRateEventInfoResponse toReservationRateEventInfoResponse(Long eventId, EventDetailRes eventDetailRes, BigDecimal reservationCnt, BigDecimal totalSeatCount) {
        Set<LocalDate> eventDateParsingList = new TreeSet<>();

        for (HashMap<Integer, EventScheduleDto> value : eventDetailRes.getScheduleMap().values()) {
            for (EventScheduleDto eventScheduleDto : value.values()) {
                eventDateParsingList.add(eventScheduleDto.getEventDate().toLocalDate());
            }
        }

        // 오늘 날짜 기준 폐막 여부 계산
        LocalDate today = LocalDate.now();
        boolean isClosed = today.isAfter(eventDateParsingList.stream().max(LocalDate::compareTo).orElse(today));

        // 예매율 계산 (reservationCnt / totalSeatCount * 100, 소수점 2자리)
        BigDecimal reservationRate = totalSeatCount.compareTo(BigDecimal.ZERO) > 0
                ? reservationCnt.divide(totalSeatCount, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
                : BigDecimal.ZERO;

        return ReservationRateEventInfoResponse
                .builder()
                .eventId(eventId)
                .eventTitle(eventDetailRes.getTitle())
                .eventMobilePosterUrl(eventDetailRes.getPosterMobileUrl())
                .eventPcPosterUrl(eventDetailRes.getPosterPcUrl())
                .eventStageName(eventDetailRes.getStageName())
                .eventDates(eventDateParsingList)
                .isClosed(isClosed)
                .reservationRate(reservationRate)
                .build();
    }
}
