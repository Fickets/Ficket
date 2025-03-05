package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.response.EventStageListResponse;
import com.example.ficketevent.domain.event.service.EventStageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventStageController {

    private final EventStageService eventStageService;


    /**
     * 행사장 전체 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-18
     * 변경 이력:
     * - 2024-11-18 오형상: 초기 작성
     */
    @GetMapping("/admin/stages")
    public ResponseEntity<EventStageListResponse> RetrieveStages() {

        EventStageListResponse response = eventStageService.getEventStages();

        return ResponseEntity.ok(response);
    }

}
