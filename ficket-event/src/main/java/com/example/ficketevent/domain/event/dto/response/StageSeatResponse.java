package com.example.ficketevent.domain.event.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class StageSeatResponse {

    private List<SeatResponse> stageSeats;

    public StageSeatResponse(List<SeatResponse> stageSeats) {
        this.stageSeats = stageSeats;
    }
}
