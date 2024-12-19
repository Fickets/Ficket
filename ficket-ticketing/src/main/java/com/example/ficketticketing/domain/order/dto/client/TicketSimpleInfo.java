package com.example.ficketticketing.domain.order.dto.client;

import lombok.Data;

import java.util.List;

@Data
public class TicketSimpleInfo {
    private List<String> seatLoc;
    private String eventTitle;
    private String stageName;
}
