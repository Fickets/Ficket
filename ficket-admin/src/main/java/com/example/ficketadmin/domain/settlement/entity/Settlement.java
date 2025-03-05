package com.example.ficketadmin.domain.settlement.entity;


import com.example.ficketadmin.global.common.BaseEntity;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Data
@Builder
@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE settlement SET deleted_at = CURRENT_TIMESTAMP WHERE settlement_id = ?")
public class Settlement extends BaseEntity {

    @Id
    @Column(name = "settlement_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementId;

    @Column(name = "net_supply_amount", nullable = false)
    private BigDecimal netSupplyAmount; // 공급가액 티켓 가격

    @Column(name = "vat", nullable = false)
    private BigDecimal vat; // 세금 > 공급가액 10%

    @Column(name = "supply_value", nullable = false)
    private BigDecimal supplyValue; // 거래가액 > 공급가액 + 세금

    @Column(name = "service_fee", nullable = false)
    private BigDecimal serviceFee; // 서비스료 > 거래가액 * 멤버쉽GRADE %

    @Column(name = "refund_value", nullable = false)
    private BigDecimal refundValue; // 환불액

    @Column(name = "settlement_value", nullable = false)
    private BigDecimal settlementValue; // 정산액

    @Column(name = "settlement_status", nullable = false)
    private SettlementStatus settlementStatus;

    @Column(name = "order_id", nullable = false)
    private Long orderId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    @JoinColumn(name = "settlement_record_id", nullable = false) // 외래키 설정
    private SettlementRecord settlementRecord;


}
