package com.example.ficketevent.domain.event.dto.common;

import lombok.Builder;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
public class TicketSimpleInfo {

    @Builder.Default
    private List<String> seatLoc = new ArrayList<>();
    private String eventTitle = "";
    private String stageName = "";
}
