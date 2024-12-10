package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TicketEventResponse {
    private LocalDateTime eventDateTime;
    private String eventStageName;
    private String eventPcBannerUrl;
    private String eventMobileBannerUrl;
    private String eventName;
    private Long companyId;
    private String seatGrade;
    private String seatRow;
    private String seatCol;
}
