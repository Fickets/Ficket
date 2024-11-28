package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.response.SeatCntByGrade;
import com.example.ficketevent.domain.event.dto.response.SeatStatusResponse;
import com.example.ficketevent.domain.event.dto.response.StageSeatResponse;
import com.example.ficketevent.domain.event.service.StageSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class StageSeatController {


    private final StageSeatService stageSeatService;

    /**
     * 해당 행사장 좌석 전체 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-18
     * 변경 이력:
     * - 2024-11-18 오형상: 초기 작성
     */
    @GetMapping("/stage/{stageId}")
    public ResponseEntity<StageSeatResponse> RetrieveSeatByStage(@PathVariable Long stageId) {
        StageSeatResponse response = stageSeatService.getSeats(stageId);

        return ResponseEntity.ok(response);
    }

    /**
     * 특정 행사 등급 별 남은 좌석 수 조회
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 오형상: 초기 작성
     */
    @GetMapping("/grades/seats/{eventScheduleId}")
    public ResponseEntity<List<SeatCntByGrade>> getRemainingSeatsByGrade(@PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(stageSeatService.getRemainingSeatsByGrade(eventScheduleId));
    }

    /**
     * 좌석 상태 전체 조회
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 오형상: 초기 작성
     */
    @GetMapping("/{eventScheduleId}/seats")
    public ResponseEntity<List<SeatStatusResponse>> getSeatStatusesByEventSchedule(@PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(stageSeatService.getSeatStatusesByEventSchedule(eventScheduleId));
    }
}
