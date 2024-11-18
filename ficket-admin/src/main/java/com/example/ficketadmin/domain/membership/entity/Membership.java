package com.example.ficketadmin.domain.membership.entity;

import com.example.ficketadmin.domain.membership.enums.Grade;
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
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE membership SET deleted_at = CURRENT_TIMESTAMP WHERE membership_id = ?")
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private BigDecimal benefit;

    private BigDecimal baseline;
}
