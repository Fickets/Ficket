package com.example.ficketadmin.domain.settlement.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class SettlementTemp {


    @Id
    @Column(name = "settlement_temp_id", nullable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long settlementTempId;

    @Column(name = "settlement_id", nullable = false)
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

    @Column(name= "is_settled", nullable = false)
    private Boolean isSettled;  // 사용자 정산 확인 여부

    @CreatedDate
    @Column(updatable = false, name = "CREATED_AT")
    private LocalDateTime createdAt; // 생성일
}
