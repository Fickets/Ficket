package com.example.ficketevent.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;


@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE company_account SET deleted_at = CURRENT_TIMESTAMP WHERE company_account_id = ?")
public class CompanyAccount extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ACCOUNT_ID")
    private Long accountId; // 계좌 ID

    @Column(name = "POINT", nullable = false)
    private BigDecimal point; // 포인트

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company; // 회사
}
