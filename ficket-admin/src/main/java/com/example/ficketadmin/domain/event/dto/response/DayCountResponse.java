package com.example.ficketadmin.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DayCountResponse {
    private Map<String, Long> dayCountMap;
}
