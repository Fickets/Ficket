package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
import com.example.ficketevent.domain.event.dto.request.EventUpdateReq;
import com.example.ficketevent.domain.event.dto.response.EventDetail;
import com.example.ficketevent.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
     */
    @PostMapping
    public ResponseEntity<String> registerEvent(@RequestPart EventCreateReq req, @RequestPart MultipartFile poster, @RequestPart MultipartFile banner) {

        eventService.createEvent(req, poster, banner);

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
     */
    @PatchMapping("/{eventId}")
    public ResponseEntity<String> modifyEvent(@PathVariable Long eventId, @RequestPart EventUpdateReq req, @RequestPart(required = false) MultipartFile poster, @RequestPart(required = false) MultipartFile banner) {

        eventService.updateEvent(eventId, req, poster, banner);

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
    @GetMapping("/{eventId}/detail")
    public ResponseEntity<EventDetail> retrieveEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(eventService.getEventById(eventId));
    }

    /**
     * 행사 삭제 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-27
     * 변경 이력:
     * - 2024-11-27 오형상: 초기 작성
     */
    @DeleteMapping("/{eventId}")
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
    @PostMapping("/content/image")
    public ResponseEntity<String> converterContentImage(@RequestPart MultipartFile image) {
        return ResponseEntity.ok(eventService.convertImageToUrl(image));
    }

}
