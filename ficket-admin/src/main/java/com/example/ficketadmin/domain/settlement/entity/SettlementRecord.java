package com.example.ficketadmin.domain.settlement.entity;

import com.example.ficketadmin.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@ToString
@Builder
@Entity
//@Table(name = "settlement_record")
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE settlement_record SET deleted_at = CURRENT_TIMESTAMP WHERE settlement_record_id = ?")
public class SettlementRecord extends BaseEntity {

    @Id
    @Column(name = "settlement_record_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
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

    @Column(name = "settlement_status")
    private SettlementStatus settlementStatus;

    @Column(name = "event_id", nullable = false)
    private Long eventId;
}
