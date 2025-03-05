package com.example.ficketadmin.domain.settlement.dto.request;


import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class SettlementReq {
    private String eventName;
    private SettlementStatus settlementStatus;
    private LocalDate startDate;
    private LocalDate endDate;
}
