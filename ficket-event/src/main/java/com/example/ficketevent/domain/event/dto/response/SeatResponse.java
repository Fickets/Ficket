package com.example.ficketevent.domain.event.dto.response;

import lombok.Data;

@Data
public class SeatResponse {

    private Long seatId; // 좌석 ID

    private Double x; // 좌표 x

    private Double y; // 좌표 y

    private String seatCol; // 열번호

    private String seatRow; // 행번호
}
