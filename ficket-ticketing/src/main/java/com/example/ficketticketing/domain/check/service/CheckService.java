package com.example.ficketticketing.domain.check.service;


import com.example.ficketticketing.domain.check.dto.CheckDto;
import com.example.ficketticketing.domain.order.entity.Ticket;
import com.example.ficketticketing.domain.order.entity.ViewingStatus;
import com.example.ficketticketing.domain.order.repository.TicketRepository;
import com.example.ficketticketing.global.result.error.ErrorCode;
import com.example.ficketticketing.global.result.error.exception.BusinessException;
import lombok.AllArgsConstructor;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@AllArgsConstructor
@Service
public class CheckService {

    private final SimpMessageSendingOperations template;
    private final TicketRepository ticketRepository;


    public void sendMessage(Long eventId, Long connectId, CheckDto message){
        String destination = String.format("/sub/check/%d/%d", eventId, connectId);
        template.convertAndSend(destination, message);
    }


    @Transactional
    public void changeTicketWatched(Long ticketId, Long eventId, Long connectId){

        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_TICKET));
        ticket.setViewingStatus(ViewingStatus.WATCHED);

        Map<String, String> message = new HashMap<>();
        message.put("message", "NEXT");
        CheckDto sendMessage = CheckDto.builder()
                .data(message)
                .build();
        sendMessage(eventId, connectId, sendMessage);
    }

}
