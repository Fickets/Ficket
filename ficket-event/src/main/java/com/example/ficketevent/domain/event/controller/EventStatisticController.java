package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.common.DailyRevenueResponse;
import com.example.ficketevent.domain.event.dto.common.DayCountResponse;
import com.example.ficketevent.domain.event.service.EventStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventStatisticController {

    private final EventStatisticService eventStatisticService;

    @GetMapping("/statistic/gender/{eventId}")
    public ResponseEntity<int[]> userStatistic(@PathVariable Long eventId) {
        int[] res = eventStatisticService.getGenderDistributionChart(eventId);

        return ResponseEntity.ok(res);
    }

    /**
     * 날짜별 수익 (admin -> event) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/daily-revenue")
    public ResponseEntity<List<DailyRevenueResponse>> calculateDailyRevenue(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventStatisticService.calculateDailyRevenue(eventId));
    }

    /**
     * 요일별 예매수 (admin -> event) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/day-count")
    public ResponseEntity<DayCountResponse> calculateDayCount(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventStatisticService.calculateDayCount(eventId));
    }
}
