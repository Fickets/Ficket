package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.service.EventStatisticService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventStatisticController {

    private final EventStatisticService eventStatisticService;

    @GetMapping("/statistic/gender/{eventId}")
    public ResponseEntity<int[]> userStatistic(@PathVariable Long eventId){
        int[] res = eventStatisticService.getGenderDistributionChart(eventId);

        return ResponseEntity.ok(res);
    }
}
