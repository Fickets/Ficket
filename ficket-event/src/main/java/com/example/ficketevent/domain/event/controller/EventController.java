package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventUpdateReq;
import com.example.ficketevent.domain.event.dto.response.*;
import com.example.ficketevent.domain.event.service.EventService;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/events")
public class EventController {

    private final EventService eventService;

    /**
     * 행사 등록 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-14
     * 변경 이력:
     * - 2024-11-14 오형상: 초기 작성
     * - 2024-11-18 오형상: company feign client 적용
     * - 2024-11-27 오형상: seatMapping 연관 관계 적용
     * - 2024-11-30 오형상: admin feign client 적용
     */
    @PostMapping("/admins/event")
    public ResponseEntity<String> registerEvent(@RequestHeader("X-Admin-Id") String adminId, @RequestPart EventCreateReq req, @RequestPart MultipartFile poster, @RequestPart MultipartFile banner) {

        eventService.createEvent(Long.parseLong(adminId), req, poster, banner);

        return ResponseEntity.ok("행사 등록에 성공했습니다.");
    }

    /**
     * 행사 수정 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-25
     * 변경 이력:
     * - 2024-11-25 오형상: 초기 작성
     * - 2024-11-27 오형상: seatMapping 연관 관계 적용
     * - 2024-11-30 오형상: admin feign client 적용
     */
    @PatchMapping("/admins/event/{eventId}")
    public ResponseEntity<String> modifyEvent(@RequestHeader("X-Admin-Id") String adminId, @PathVariable Long eventId, @RequestPart EventUpdateReq req, @RequestPart(required = false) MultipartFile poster, @RequestPart(required = false) MultipartFile banner) {

        eventService.updateEvent(eventId, Long.parseLong(adminId), req, poster, banner);

        return ResponseEntity.ok("행사 정보 수정에 성공했습니다.");
    }

    /**
     * 행사 단건 조회 API (행사 수정 용)
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-25
     * 변경 이력:
     * - 2024-11-25 오형상: 초기 작성
     */
    @GetMapping("/admin/{eventId}/detail")
    public ResponseEntity<EventDetail> retrieveEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    /**
     * 공연 리스트 검색 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-03
     * 변경 이력:
     * - 2024-12-03 오형상: 초기 작성
     */
    @GetMapping("/admin")
    public ResponseEntity<PagedResponse<EventSearchListRes>> searchEvent(
            EventSearchCond eventSearchCond,
            Pageable pageable) {
        return ResponseEntity.ok(eventService.searchEvent(eventSearchCond, pageable));
    }

    /**
     * 행사 삭제 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-27
     * 변경 이력:
     * - 2024-11-27 오형상: 초기 작성
     */
    @DeleteMapping("/admin/{eventId}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long eventId) {
        eventService.deleteEvent(eventId);
        return ResponseEntity.noContent().build();
    }

    /**
     * 행사 내용 이미지 URL 변환 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-20
     * 변경 이력:
     * - 2024-11-20 오형상: 초기 작성
     */
    @PostMapping("/admin/content/image")
    public ResponseEntity<String> converterContentImage(@RequestPart MultipartFile image) {
        return ResponseEntity.ok(eventService.convertImageToUrl(image));
    }

    /**
     * 행사 정보 조회 API (좌석 선택용)
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-27
     * 변경 이력:
     * - 2024-11-27 오형상: 초기 작성
     */
    @GetMapping("/event-simple/{eventScheduleId}")
    public ResponseEntity<EventSeatSummary> getEventSeatSummary(@PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(eventService.getEventByScheduleId(eventScheduleId));
    }


    @GetMapping("detail/{eventId}")
    public ResponseEntity<EventDetailRes> T(@PathVariable Long eventId) {
        EventDetailRes res = eventService.getEventDetail(eventId);
        return ResponseEntity.ok(res);

    }

    @GetMapping("{eventId}/reservation")
    public ResponseEntity<Integer> TT() {
        return ResponseEntity.status(HttpResponseStatus.OK.code()).build();

    }

}
