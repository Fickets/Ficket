package com.example.ficketadmin.domain.settlement.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Builder
@Entity
public class SettlementRecordTemp {


    @Id
    @Column(name = "settlement_record_temp_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementRecordTempId;

    @Column(name = "settlement_record_id", nullable = false)
    private Long settlementRecordId;


    @Column(name = "total_net_supply_amount", nullable = false)
    private BigDecimal totalNetSupplyAmount;


    @Column(name = "total_vat", nullable = false)
    private BigDecimal totalVat;

    @Column(name = "total_supply_amount", nullable = false)
    private BigDecimal totalSupplyAmount;


    @Column(name = "total_service_fee", nullable = false)
    private BigDecimal totalServiceFee;


    @Column(name = "total_settlement_value", nullable = false)
    private BigDecimal totalSettlementValue;


    @Column(name = "total_refund_value", nullable = false)
    private BigDecimal totalRefundValue;

    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "event_id", nullable = false)
    private Long eventId;

    @Column(name= "is_settled", nullable = false)
    private Boolean isSettled;  // 사용자 정산 확인 여부

    @CreatedDate
    @Column(updatable = false, name = "CREATED_AT")
    private LocalDateTime createdAt; // 생성일
}
