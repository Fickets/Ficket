package com.example.ficketadmin.domain.membership.entity;

import com.example.ficketadmin.domain.membership.enums.Grade;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long membershipId;

    @Enumerated(EnumType.STRING)
    private Grade grade;

    private BigDecimal benefit;

    private BigDecimal baseline;
}
