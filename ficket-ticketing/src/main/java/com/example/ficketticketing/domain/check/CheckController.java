package com.example.ficketticketing.domain.check;


import com.example.ficketticketing.domain.order.dto.client.FaceApiResponse;
import com.example.ficketticketing.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/ticketing/check")
public class CheckController {

    private final OrderService orderService;

    @PostMapping("{eventScheduleId}/user-match")
    public ResponseEntity<FaceApiResponse> userMatch(@RequestPart MultipartFile userImg, @PathVariable Long eventScheduleId) {
        return ResponseEntity.ok(orderService.matchFace(userImg, eventScheduleId));
    }

}
