package com.example.ficketadmin.domain.event.controller;


import com.example.ficketadmin.domain.event.dto.response.TemporaryUrlResponse;
import com.example.ficketadmin.domain.event.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class EventController {

    private final EventService eventService;

    //TODO   기능                 메소드/담당     path:/api/v1/admins
    
    //TODO  성별예매율조회           /GET/OHS     /{eventId}/gender-ratio
    //TODO  연령별예매율조회         /GET/OHS      /{eventId}/age-ratio
    //TODO  날짜별예매율조회         /GET/OHS      /{eventId}/daily-revenue
    //TODO  날짜별수익조회           /GET/OHS      /{eventId}/day-count
    //TODO  요일별예매수조회         /GET/OHS       /{eventId}/day-count
    //TODO  임시URL검증           /GET/CYS         /validation/{uuid}


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

}
