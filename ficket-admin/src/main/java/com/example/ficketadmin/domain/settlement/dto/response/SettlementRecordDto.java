package com.example.ficketadmin.domain.settlement.dto.response;

import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import com.querydsl.core.annotations.QueryProjection;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementRecordDto {
    private Long eventId;
    private LocalDateTime createdAt;
    private LocalDateTime lastModifiedAt;
    private BigDecimal totalNetSupplyAmount;
    private BigDecimal totalServiceFee;
    private BigDecimal totalSettlementValue;
    private SettlementStatus settlementStatus;
    private BigDecimal totalSupplyAmount;
    private String title;
    private String companyName;
    private Long companyId;

    public SettlementRecordDto(Long eventId, LocalDateTime createdAt, LocalDateTime lastModifiedAt, BigDecimal totalNetSupplyAmount, BigDecimal totalServiceFee, BigDecimal totalSettlementValue, BigDecimal totalSupplyAmount, SettlementStatus settlementStatus, String title, String companyName, Long companyId) {
        this.eventId = eventId;
        this.createdAt = createdAt;
        this.lastModifiedAt = lastModifiedAt;
        this.totalNetSupplyAmount = totalNetSupplyAmount;
        this.totalServiceFee = totalServiceFee;
        this.totalSettlementValue = totalSettlementValue;
        this.totalSupplyAmount=totalSupplyAmount;
        this.settlementStatus = settlementStatus;
        this.title = title;
        this.companyId=companyId;
        this.companyName=companyName;
    }

    @QueryProjection
    public SettlementRecordDto(Long eventId, LocalDateTime createdAt, LocalDateTime lastModifiedAt,BigDecimal totalNetSupplyAmount,BigDecimal totalServiceFee,BigDecimal totalSettlementValue, BigDecimal totalSupplyAmount, SettlementStatus settlementStatus){
        this.eventId=eventId;
        this.createdAt=createdAt;
        this.lastModifiedAt=lastModifiedAt;
        this.totalNetSupplyAmount=totalNetSupplyAmount;
        this.totalServiceFee=totalServiceFee;
        this.totalSettlementValue=totalSettlementValue;
        this.totalSupplyAmount = totalSupplyAmount;
        this.settlementStatus=settlementStatus;
    }
}
