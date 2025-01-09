package com.example.ficketticketing.domain.check.controller;


import com.example.ficketticketing.domain.check.dto.CheckDto;
import com.example.ficketticketing.domain.check.service.CheckService;
import com.example.ficketticketing.domain.order.dto.client.FaceApiResponse;
import com.example.ficketticketing.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticketing/check")
public class CheckController {

    private final OrderService orderService;
    private final CheckService checkService;

    @PostMapping("/{eventId}/user-match")
    public ResponseEntity<Void> userMatch(@RequestPart MultipartFile userImg, @PathVariable Long eventId, @PathVariable Long connectId) {
        return ResponseEntity.noContent().build();
    }

    @ResponseBody
    @MessageMapping("/message/{eventId}/{connectId}")
    public void sendMessage(@Header("Authorization") String headerToken, @DestinationVariable("eventId") Long eventId, @DestinationVariable("connectId") Long connectId, @Payload String message) {
        CheckDto checkDto =  CheckDto.builder()
                .data(message)
                .seatLoc(null)
                .birth(1)
                .name("TEST")
                .build();
        checkService.sendMessage(eventId, connectId, checkDto);
    }


}
