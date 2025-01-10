package com.example.ficketadmin.domain.check.service;


import com.example.ficketadmin.domain.check.client.EventServiceClient;
import com.example.ficketadmin.domain.check.client.FaceServiceClient;
import com.example.ficketadmin.domain.check.client.TicketingServiceClient;
import com.example.ficketadmin.domain.check.dto.CheckDto;
import com.example.ficketadmin.domain.check.dto.FaceApiResponse;
import com.example.ficketadmin.domain.check.dto.TicketSimpleInfo;
import com.example.ficketadmin.domain.check.dto.UserSimpleDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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


    public void matchFace(MultipartFile userFaceImage, Long eventId, Long connectId) {
        List<Long> eventScheduleIds = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getEventScheduleIdList",
                () -> eventServiceClient.getScheduledId(eventId));

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

                ObjectMapper objectMapper = new ObjectMapper();
                Map<String, Object> map = objectMapper.convertValue(faceApiResponse.getData(), Map.class);
                Long ticketId = ((Number) map.get("ticket_id")).longValue();
                TicketSimpleInfo ticketSimpleInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getCustomerSeat",
                        () -> eventServiceClient.getTicketSimpleInfo(ticketId));

                // ticketId 로 userId 가져와야함
                UserSimpleDto userInfo = executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getCustomerSeat",
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


    @Transactional
    public void changeTicketWatched(Long ticketId, Long eventId, Long connectId){

        executeWithCircuitBreaker(circuitBreakerRegistry,
                "getCustomerSeat",
                () -> ticketingServiceClient.changeTicketWatched(ticketId));

        Map<String, String> message = new HashMap<>();
        message.put("message", "NEXT");
        CheckDto sendMessage = CheckDto.builder()
                .data(message)
                .build();
        sendMessage(eventId, connectId, sendMessage);
    }









}
