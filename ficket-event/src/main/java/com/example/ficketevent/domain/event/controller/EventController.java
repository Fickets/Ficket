package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.common.EventTitleDto;
import com.example.ficketevent.domain.event.dto.common.TicketInfoCreateDtoList;
import com.example.ficketevent.domain.event.dto.common.TicketInfoDto;
import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
import com.example.ficketevent.domain.event.dto.request.EventScheduledOpenSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.request.EventUpdateReq;
import com.example.ficketevent.domain.event.dto.response.*;
import com.example.ficketevent.domain.event.enums.Genre;
import com.example.ficketevent.domain.event.enums.Period;
import com.example.ficketevent.domain.event.service.EventService;
import io.netty.handler.codec.http.HttpResponseStatus;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
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
     * - 2024-12-26 오형상: event 생성시 오픈 티켓 캐시 삭제 적용
     * - 2025-02-22 오형상: xss 필터링 적용
     */
    @PostMapping("/admins/event")
    public ResponseEntity<String> registerEvent(@RequestHeader("X-Admin-Id") String adminId, @RequestPart EventCreateReq req, @RequestPart MultipartFile poster, @RequestPart MultipartFile banner) {
        req.sanitizeContent(); // XSS 필터 적용
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
     * - 2024-12-21 오형상: 수동 캐시 삭제 적용
     * - 2024-12-24 오형상: 페이징 캐시 삭제 적용
     * - 2024-12-30 오형상: 스케쥴, 좌석 매칭 오류 해결
     * - 2025-02-22 오형상: xss 필터링 적용
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
     * - 2024-12-30 오형상: 설정 안된 좌석 구분 안 보이게 로직 추가
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
     * - 2024-12-16 오형상: 랭킹 삭제 로직 추가
     * - 2024-12-21 오형상: 수동 캐시 삭제 적용
     * - 2024-12-24 오형상: 페이징 캐시 삭제 적용
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

    /**
     * 행사 상세 정보 조회 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-03
     * 변경 이력:
     * - 2024-12-03 최용수: 초기 작성
     * - 2024-12-13 오형상: 캐시, 조회수 랭킹 적용
     */
    @GetMapping("detail/{eventId}")
    public ResponseEntity<EventDetailRes> T(HttpServletRequest request, HttpServletResponse response, @PathVariable Long eventId) {
        EventDetailRes res = eventService.getEventDetail(request, response, eventId);
        return ResponseEntity.ok(res);

    }


    @GetMapping("{eventId}/reservation")
    public ResponseEntity<Integer> TT() {
        return ResponseEntity.status(HttpResponseStatus.OK.code()).build();

    }

    /**
     * 티켓 정보 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-09
     * 변경 이력:
     * - 2024-12-09 오형상: 초기 작성
     */
    @PostMapping("/my-ticket-info")
    public ResponseEntity<List<TicketInfoDto>> getMyTicketInfo(@RequestBody TicketInfoCreateDtoList ticketInfoCreateDtoList) {
        return ResponseEntity.ok(eventService.getMyTicketInfo(ticketInfoCreateDtoList));
    }

    /**
     * 공연 날짜 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-10
     * 변경 이력:
     * - 2024-12-10 오형상: 초기 작성
     */
    @GetMapping("/date/time/{eventScheduleId}")
    public ResponseEntity<LocalDateTime> getEventDateTime(@PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(eventService.getEventDateTime(eventScheduleId));
    }

    /**
     * 조회수 기준 랭킹 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-14
     * 변경 이력:
     * - 2024-12-14 오형상: 초기 작성
     */
    @GetMapping("/detail/view-rank")
    public ResponseEntity<List<ViewRankResponse>> getViewRank(@RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(eventService.getTopRankedEvents(limit));
    }

    /**
     * 예매율 기준 랭킹 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-15
     * 변경 이력:
     * - 2024-12-15 오형상: 초기 작성
     * - 2024-12-28 오형상: 상위 50위 고정에서 상위 N위 동적으로 변경
     */
    @GetMapping("/detail/reservation-rate-rank")
    public ResponseEntity<List<ReservationRateEventInfoResponse>> getReservationRateRank(
            @RequestParam(defaultValue = "뮤지컬") Genre genre,
            @RequestParam(defaultValue = "DAILY") Period period,
            @RequestParam(defaultValue = "50") int topN
    ) {
        return ResponseEntity.ok(eventService.getTopNReservationRateRank(genre, period, topN));
    }

    /**
     * 오늘 예매 가능한 이벤트 ID 리스트 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     */
    @GetMapping("/today-open-events")
    public ResponseEntity<List<Long>> getTodayOpenEvents() {
        return ResponseEntity.ok(eventService.getTodayOpenEvents());
    }

    /**
     * 어제자 예매 이벤트 ID 리스트 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-19
     * 변경 이력:
     * - 2024-12-19 오형상: 초기 작성
     */
    @GetMapping("/yesterday-open-events")
    public ResponseEntity<List<Long>> getYesterdayOpenEvents() {
        return ResponseEntity.ok(eventService.getYesterdayOpenEvents());
    }

    /**
     * 오픈 예정 행사 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-24
     * 변경 이력:
     * - 2024-12-24 오형상: 초기 작성
     * - 2024-12-26 오형상: totalPages 누락
     */
    @GetMapping("/detail/scheduled-open-event")
    public ResponseEntity<PageDTO<EventScheduledOpenResponse>> searchOpenEvent(
            EventScheduledOpenSearchCond eventScheduledOpenSearchCond,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(eventService.searchOpenEvent(eventScheduledOpenSearchCond, pageable));
    }

    /**
     * 이벤트,회사 PK 조회 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-26
     * 변경 이력:
     * - 2024-12-26 최용수: 초기 작성
     */
    @GetMapping("/company-id")
    public List<Long> getCompanyId(@RequestParam Long ticketId) {
        return eventService.getCompanyId(ticketId);
    }

    /**
     * 제목 포함 이벤트 찾기 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-26
     * 변경 이력:
     * - 2024-12-26 최용수: 초기 작성
     */
    @GetMapping("/search-ids")
    public List<EventTitleDto> searchIds(@RequestParam String title) {
        return eventService.getTitleIds(title);
    }

    /**
     * 모든 이벤트 제목 조회 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-26
     * 변경 이력:
     * - 2024-12-26 최용수: 초기 작성
     */
    @GetMapping("/search-title")
    public List<String> searchTitle() {
        return eventService.getTitle();
    }

    /**
     * 오픈 티켓 최신 6개  조회 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-27
     * 변경 이력:
     * - 2024-12-27 최용수: 초기 작성
     * - 2024-12-28 최용수: 장르선택 적용
     */
    @GetMapping("/open-recent")
    public ResponseEntity<List<SimpleEvent>> getOpenRecent(@RequestParam(name = "genre", required = false) String genre) {
        List<SimpleEvent> res = eventService.getOpenRecent(genre);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/genre-rank")
    public ResponseEntity<List<SimpleEvent>> getGenreRank(@RequestParam(name = "genre") String genre) {
        List<SimpleEvent> res = eventService.getGenreRank(genre);
        return ResponseEntity.ok(res);
    }

    @GetMapping("/area")
    public ResponseEntity<List<String>> getAllArea() {
        List<String> res = eventService.allArea();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/genre-search")
    public ResponseEntity<SimplePageRes> getGenreSearch(
            @RequestParam(defaultValue = "뮤지컬") Genre genre,
            @RequestParam(defaultValue = "") String area,
            @RequestParam(defaultValue = "DAILY") Period period,
            @PageableDefault(page = 0, size = 10, sort = "eventDate", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(eventService.getGenreList(genre, area, period, pageable));
    }

    @GetMapping("/events/getScheduleId")
    List<Long> getScheduledId(@RequestParam Long eventId) {
        return eventService.getScheduleId(eventId);
    }

    @GetMapping("/companyId/{eventId}")
    public Long getCompanyIdByEventId(@PathVariable(name = "eventId") Long eventId) {
        return eventService.getCompanyByEvent(eventId);
    }

    @GetMapping("/check-time/{eventId}")
    public ResponseEntity<Boolean> checkEventTicketingTime(@PathVariable(name = "eventId") Long eventId) {
        Boolean result = eventService.getCheckTicketingTime(eventId);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/check-schedule/{scheduleId}")
    public ResponseEntity<Boolean> checkEventScheduleTime(@PathVariable(name = "scheduleId") Long scheduleId) {
        Boolean Result = eventService.getCheckScheduleTime(scheduleId);
        return ResponseEntity.ok(Result);
    }
}
