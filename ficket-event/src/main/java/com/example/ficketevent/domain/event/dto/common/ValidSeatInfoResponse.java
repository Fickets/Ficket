package com.example.ficketevent.domain.event.dto.common;

import com.example.ficketevent.domain.event.dto.request.SelectSeatInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ValidSeatInfoResponse {
    private Integer reservationLimit;
    private Set<SelectSeatInfo> selectSeatInfoList;
}
