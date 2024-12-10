package com.example.ficketticketing.domain.order.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoDto {
    private LocalDateTime createdAt;
    private LocalDateTime eventDateTime;
    private String eventStageName;
    private String eventPcBannerUrl;
    private String eventMobileBannerUrl;
    private String eventName;
    private String companyName;
    private String seatGrade;
    private String seatRow;
    private String seatCol;
    private String sido;
}
