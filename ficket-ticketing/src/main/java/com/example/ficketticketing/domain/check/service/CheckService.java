package com.example.ficketticketing.domain.check.service;


import com.example.ficketticketing.domain.check.dto.CheckDto;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;

@AllArgsConstructor
@Service
public class CheckService {

    private final SimpMessageSendingOperations template;

    public void sendMessage(Long eventId, Long connectId, CheckDto message){
        String destination = String.format("/sub/check/%d/%d", eventId, connectId);
        template.convertAndSend(destination, message);
    }

}
