package com.example.ficketadmin.domain.settlement.dto.response;

import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@Builder
public class SettlementRecordDto {
    private Long eventId;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private BigDecimal totalNetSupplyAmount;
    private BigDecimal totalServiceFee;
    private BigDecimal totalSettlementValue;
    private SettlementStatus settlementStatus;
    private String title;

    public SettlementRecordDto(Long eventId, LocalDateTime createdAt, LocalDateTime lastModifiedAt, BigDecimal totalNetSupplyAmount, BigDecimal totalServiceFee, BigDecimal totalSettlementValue, SettlementStatus settlementStatus, String title) {
        this.eventId = eventId;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.totalNetSupplyAmount = totalNetSupplyAmount;
        this.totalServiceFee = totalServiceFee;
        this.totalSettlementValue = totalSettlementValue;
        this.settlementStatus = settlementStatus;
        this.title = title;
    }

    @QueryProjection
    public SettlementRecordDto(Long eventId, LocalDateTime createdAt, LocalDateTime lastModifiedAt,BigDecimal totalNetSupplyAmount,BigDecimal totalServiceFee,BigDecimal totalSettlementValue, SettlementStatus settlementStatus){
        this.eventId=eventId;
        this.createdAt=createdAt;
        this.lastModifiedAt=lastModifiedAt;
        this.totalNetSupplyAmount=totalNetSupplyAmount;
        this.totalServiceFee=totalServiceFee;
        this.totalSettlementValue=totalSettlementValue;
        this.settlementStatus=settlementStatus;
    }
}
