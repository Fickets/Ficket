package com.example.ficketadmin.domain.event.controller;


import com.example.ficketadmin.domain.event.dto.response.DailyRevenueResponse;
import com.example.ficketadmin.domain.event.dto.response.DayCountResponse;
import com.example.ficketadmin.domain.event.dto.response.GuestTokenResponse;
import com.example.ficketadmin.domain.event.dto.response.TemporaryUrlResponse;
import com.example.ficketadmin.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class EventController {

    private final EventService eventService;

    /**
     * 날짜 별 수익 (admin -> event) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/daily-revenue")
    public ResponseEntity<List<DailyRevenueResponse>> calculateDailyRevenue(@PathVariable Long eventId) {
        List<DailyRevenueResponse> dailyRevenueResponseList = eventService.calculateDailyRevenue(eventId);
        return ResponseEntity.ok(dailyRevenueResponseList);
    }

    /**
     * 요일 별 예매 수 (admin -> event) API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-12
     * 변경 이력:
     * - 2024-12-12 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/day-count")
    public ResponseEntity<DayCountResponse> calculateDayCount(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.calculateDayCount(eventId));
    }

    /**
     * 임시 URL 발급 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-09
     * 변경 이력:
     * - 2024-12-09 오형상: 초기 작성
     */
    @PostMapping("/generate-url")
    public TemporaryUrlResponse generateUrl(@RequestParam Long eventId) {
        return eventService.generateTemporaryUrl(eventId);
    }

    /**
     * 특정 이벤트 ID에 대해 임시 URL이 존재하는지 확인하는 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-02-17
     * 변경 이력:
     * - 2025-02-17 오형상: 초기 작성
     */
    @GetMapping("/{eventId}/temporary-url/exists")
    public TemporaryUrlResponse checkTemporaryUrlExists(@PathVariable Long eventId) {
        return eventService.checkTemporaryUrlExists(eventId);
    }

    @GetMapping("/check-url/{eventId}")
    public GuestTokenResponse checkUrl(@PathVariable(name = "eventId") Long eventId, @RequestParam(name = "uuid") String uuid) {
        return eventService.checkUrl(eventId, uuid);
    }



    /**
     * 해당 이벤트의 슬롯 초기화 API (ADMIN -> QUEUE)
     *
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-24
     * 변경 이력:
     * - 2025-01-24 오형상: 초기 작성
     */
    @PostMapping("/{eventId}/initialize-slot")
    public ResponseEntity<String> initializeSlot(@PathVariable String eventId, @RequestParam int maxSlot) {
        eventService.initializeSlot(eventId, maxSlot);
        return ResponseEntity.ok("슬롯 초기화 성공!");
    }

    /**
     * 해당 이벤트의 슬롯 제거 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2025-01-24
     * 변경 이력:
     * - 2025-01-24 오형상: 초기 작성
     */
    @DeleteMapping("/{eventId}/delete-slot")
    public ResponseEntity<Void> removeSlot(@PathVariable String eventId) {
        eventService.deleteSlot(eventId);
        return ResponseEntity.noContent().build();
    }

}
