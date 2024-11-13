package com.example.ficketevent.Entity;

import com.example.ficketevent.enums.Grade;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE membership SET deleted_at = CURRENT_TIMESTAMP WHERE membership_id = ?")
public class Membership extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "MEMBERSHIP_ID")
    private Long membershipId; // 멤버십 ID

    @Enumerated(EnumType.STRING)
    @Column(name = "GRADE", nullable = false)
    private Grade grade; // 등급

    @Column(name = "BENEFIT", nullable = false)
    private Integer benefit; // 혜택

    @Column(name = "BASELINE", nullable = false)
    private BigDecimal baseline; // 기준

}
