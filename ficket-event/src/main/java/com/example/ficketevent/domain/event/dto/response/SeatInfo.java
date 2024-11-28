package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatInfo {
    private Long seatMappingId; // 좌석 매핑 ID
    private Double seatX;
    private Double seatY;
    private String seatGrade;
    private String seatRow;
    private String seatCol;
    private Boolean purchased;
}