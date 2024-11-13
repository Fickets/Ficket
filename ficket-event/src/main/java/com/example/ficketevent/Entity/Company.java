package com.example.ficketevent.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.List;

/**
 * 회사 정보를 저장하는 엔티티 클래스
 */
@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE company SET deleted_at = CURRENT_TIMESTAMP WHERE company_id = ?")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "COMPANY_ID")
    private Long companyId; // 회사 ID

    @Column(name = "COMPANY_NAME", nullable = false)
    private String companyName; // 회사 이름

    @Column(name = "REVENUE", nullable = false)
    private BigDecimal revenue; // 수익

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "MEMBERSHIP_ID", nullable = false)
    private Membership membership; // 멤버십

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CompanyAccount> accounts; // 회사 계좌 목록

    @OneToMany(mappedBy = "company", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events; // 이벤트 목록

}
