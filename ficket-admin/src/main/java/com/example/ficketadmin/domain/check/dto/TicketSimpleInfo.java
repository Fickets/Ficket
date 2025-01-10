package com.example.ficketadmin.domain.check.dto;

import lombok.Data;

import java.util.List;

@Data
public class TicketSimpleInfo {
    private List<String> seatLoc;
    private String eventTitle;
    private String stageName;
}
