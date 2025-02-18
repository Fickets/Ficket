package com.example.ficketadmin.domain.check.controller;


import com.example.ficketadmin.domain.check.service.CheckService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins/check")
public class CheckController {

    private final CheckService checkService;

    @PostMapping("/{eventId}/{connectId}/user-match")
    public ResponseEntity<Void> userMatch(@RequestPart MultipartFile userImg, @PathVariable(name = "eventId") Long eventId, @PathVariable(name = "connectId") Long connectId) {
        checkService.matchFace(userImg,eventId, connectId);
        return ResponseEntity.noContent().build();
    }


    /**
     * 티켓 확인 처리
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-12-14
     * 변경 이력:
     * - 2024-12-14 최용수: 초기 작성
     */
    @GetMapping("/ticket-watch/{ticketId}")
    public ResponseEntity<Void> ticketWatchedChange(@PathVariable(name = "ticketId")Long ticketId,
                                                    @RequestParam(name = "eventId")Long eventId,
                                                    @RequestParam(name = "connectId")Long connectId) {
        checkService.changeTicketWatched(ticketId, eventId, connectId);
        return ResponseEntity.noContent().build();
    }
}
