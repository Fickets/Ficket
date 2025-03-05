package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.common.*;
import com.example.ficketevent.domain.event.dto.request.SelectSeat;
import com.example.ficketevent.domain.event.dto.request.UnSelectSeat;
import com.example.ficketevent.domain.event.dto.response.SeatCntByGrade;
import com.example.ficketevent.domain.event.dto.response.SeatStatusResponse;
import com.example.ficketevent.domain.event.dto.response.StageSeatResponse;
import com.example.ficketevent.domain.event.service.PreoccupyService;
import com.example.ficketevent.domain.event.service.StageSeatService;
import jakarta.ws.rs.QueryParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class StageSeatController {

    private final StageSeatService stageSeatService;
    private final PreoccupyService preoccupyService;

    /**
     * 해당 행사장 좌석 전체 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-18
     * 변경 이력:
     * - 2024-11-18 오형상: 초기 작성
     */
    @GetMapping("/admin/stage/{stageId}")
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

    /**
     * 좌석 선점 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 오형상: 초기 작성
     */
    @PostMapping("/seat/lock")
    public ResponseEntity<Void> lockSeats(@RequestBody SelectSeat req, @RequestHeader("X-User-Id") String userId) {
        preoccupyService.lockSeat(req, Long.parseLong(userId));
        return ResponseEntity.ok().build();
    }

    /**
     * 좌석 선점 해제 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 오형상: 초기 작성
     */
    @PostMapping("/seat/unlock")
    public ResponseEntity<Void> releaseLockedSeats(@RequestBody UnSelectSeat req, @RequestHeader("X-User-Id") String userId) {
        preoccupyService.releaseSeat(req.getEventScheduleId(), req.getSeatMappingIds(), Long.parseLong(userId));
        return ResponseEntity.ok().build();
    }

    /**
     * 좌석 선점 여부 검증 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-07
     * 변경 이력:
     * - 2024-12-07 오형상: 초기 작성
     */
    @GetMapping
    public ResponseEntity<ReservedSeatsResponse> getReservedSeats(@QueryParam("userId") Long userId, @QueryParam("eventScheduleId") Long eventScheduleId) {
        return ResponseEntity.ok(stageSeatService.getReservedSeats(userId, eventScheduleId));
    }

    /**
     * 결제 정보 검증 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-08
     * 변경 이력:
     * - 2024-12-08 오형상: 초기 작성
     */
    @PostMapping("/valid-request")
    ResponseEntity<ValidSeatInfoResponse> checkRequest(@RequestBody CreateOrderRequest createOrderRequest) {
        return ResponseEntity.ok(stageSeatService.validRequest(createOrderRequest));
    }

    /**
     * 좌석 재 오픈 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-10
     * 변경 이력:
     * - 2024-12-10 오형상: 초기 작성RedisKeyHelper
     * - 2024-12-14 오형상: 환불 시 예매율 순위 업데이트 로직 추가
     */
    @PostMapping("/refund")
    ResponseEntity<Void> refundTicket(@RequestBody TicketInfo ticketInfo) {
        stageSeatService.openSeat(ticketInfo);
        return ResponseEntity.ok().build();
    }


    /**
     * 해당 유저의 해당 회차 구매 가능 티켓 수 반환 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-11
     * 변경 이력:
     * - 2024-12-11 오형상: 초기 작성
     */
    @PostMapping("/available-count")
    ResponseEntity<Integer> getAvailableCount(@RequestBody TicketDto ticketDto) {
        return ResponseEntity.ok(stageSeatService.getAvailableCount(ticketDto));
    }

    /**
     * 티켓아이디로 이벤트정보, 좌석정보 반환 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 최용수: 초기 작성
     */
    @GetMapping("/customer-ticket-info")
    public TicketSimpleInfo getTicketSimpleInfo(@RequestParam Long ticketId) {
        return stageSeatService.getTicketSimpleInfo(ticketId);
    }

    @GetMapping("/count-ticket/{ticketId}")
    public Long getBuyTicketCount(@PathVariable Long ticketId) {
        return stageSeatService.ticketSeatCount(ticketId);
    }

    /**
     * 해당 유저의 좌석 선점 해제 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-02-20
     * 변경 이력:
     * - 2025-02-20 오형상: 초기 작성
     */
    @DeleteMapping("/unlock-seats")
    ResponseEntity<Void> unLockSeatByEventScheduleIdAndUserId(
            @RequestParam("eventScheduleId") String eventScheduleId,
            @RequestParam("userId") String userId) {
        preoccupyService.unLockSeatByEventScheduleIdAndUserId(eventScheduleId, userId);
        return ResponseEntity.ok().build();
    }

    /**
     * 좌석 락으로 부터 userId 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-02-21
     * 변경 이력:
     * - 2025-02-21 오형상: 초기 작성
     */
    @GetMapping("/userId")
    ResponseEntity<String> getUserIdBySeatLock(
            @RequestParam("eventScheduleId") String eventScheduleId,
            @RequestParam("seatMappingId") String seatMappingId) {
        return ResponseEntity.ok(preoccupyService.getUserIdBySeatLock(eventScheduleId, seatMappingId));
    }
}
