package com.example.ficketuser.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MyTicketResponse {
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime eventDateTime;
    private String eventStageName;
    private String sido;
    private String eventPcBannerUrl;
    private String eventMobileBannerUrl;
    private String eventName;
    private String companyName;
    private List<MySeatInfo> mySeatInfoList;
}
