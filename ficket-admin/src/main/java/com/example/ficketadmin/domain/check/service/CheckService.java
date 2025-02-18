package com.example.ficketadmin.domain.check.service;


import com.example.ficketadmin.domain.check.client.FaceServiceClient;
import com.example.ficketadmin.domain.check.dto.CheckDto;
import com.example.ficketadmin.domain.check.dto.FaceApiResponse;
import com.example.ficketadmin.domain.check.dto.TicketSimpleInfo;
import com.example.ficketadmin.domain.check.dto.UserSimpleDto;
import com.example.ficketadmin.domain.event.client.EventServiceClient;
import com.example.ficketadmin.domain.event.client.TicketingServiceClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.ficketadmin.global.utils.CircuitBreakerUtils.executeWithCircuitBreaker;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CheckService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final EventServiceClient eventServiceClient;
    private final FaceServiceClient faceServiceClient;
    private final TicketingServiceClient ticketingServiceClient;
    private final SimpMessageSendingOperations template;


    public void sendMessage(Long eventId, Long connectId, CheckDto message){
        String destination = String.format("/sub/check/%d/%d", eventId, connectId);
        template.convertAndSend(destination, message);
    }

    public static String convertToBase64(MultipartFile file) throws IOException {
        byte[] fileBytes = file.getBytes();  // 파일의 바이너리 데이터 가져오기
        return Base64.getEncoder().encodeToString(fileBytes);  // Base64 문자열로 변환
    }

    public void matchFace(MultipartFile userFaceImage, Long eventId, Long connectId) {
        log.info("TEST MATCH FACE START");
        try {
            String imageString = convertToBase64(userFaceImage);
            log.info(imageString);

        }catch (IOException e){
            log.info("CHANGE FAIL");
        }


        List<Long> eventScheduleIds = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getEventScheduleIdList",
                () -> eventServiceClient.getScheduledId(eventId));
        log.info("EVENT IDS FIND : " + eventScheduleIds.size());
        for (Long eventScheduleId : eventScheduleIds) {
            FaceApiResponse faceApiResponse = null;
            try{
                faceApiResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "postMatchUserFaceImgCircuitBreaker",
                        () -> faceServiceClient.matchFace(userFaceImage, eventScheduleId)
                );
            } catch (Exception e){
                log.info(e.toString());
            }

            if (faceApiResponse != null && faceApiResponse.getStatus() == 200) {
                log.info("FOUND FACE SUCCESS");
                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> map = objectMapper.convertValue(faceApiResponse.getData(), Map.class);
                Long ticketId = ((Number) map.get("ticket_id")).longValue();
                TicketSimpleInfo ticketSimpleInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getSimpleTicketInfo",
                        () -> eventServiceClient.getTicketSimpleInfo(ticketId));

                // ticketId 로 userId 가져와야함
                UserSimpleDto userInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getUserIdByTicketId",
                        () -> ticketingServiceClient.getUserIdByTicketId(ticketId));

                CheckDto message = CheckDto.builder()
                        .data(faceApiResponse.getData())
                        .name(userInfo.getUserName())
                        .birth(userInfo.getBirth())
                        .seatLoc(ticketSimpleInfo.getSeatLoc())
                        .build();
                sendMessage(eventId, connectId, message);
                break;
            }
        }
    }


    @CircuitBreaker(name = "changeTicketToWatched")
    public void changeTicketWatched(Long ticketId, Long eventId, Long connectId){
        ticketingServiceClient.changeTicketWatched(ticketId);

        Map<String, String> message = new HashMap<>();
        message.put("message", "NEXT");
        CheckDto sendMessage = CheckDto.builder()
                .data(message)
                .build();
        sendMessage(eventId, connectId, sendMessage);
    }

}
