package com.example.ficketuser.dto.client;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderInfoDto {

    private Long orderId;
    private List<String> seatLoc;
    private BigDecimal ticketTotalPrice;
    private String eventTitle;
    private String stageName;
    private LocalDateTime createdAt;

}
