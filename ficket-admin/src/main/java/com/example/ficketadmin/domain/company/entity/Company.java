package com.example.ficketadmin.domain.company.entity;

import com.example.ficketadmin.domain.account.entity.Account;
import com.example.ficketadmin.domain.membership.entity.Membership;
import com.example.ficketadmin.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "company_id", nullable = false)
    private Long companyId; // 회사 ID

    @Column(name = "company_name", nullable = false, length = 50)
    private String companyName; // 회사 이름

    @Column(name = "revenue", nullable = false)
    private BigDecimal revenue; // 수익

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true) // 단방향 1:1 관계
    @JoinColumn(name = "account_id", nullable = false) // 외래 키 설정
    private Account account; // 회사 계좌 정보

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "  ", nullable = false)
    private Membership membership; // 멤버쉽

}
