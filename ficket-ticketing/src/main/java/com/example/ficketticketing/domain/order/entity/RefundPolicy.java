package com.example.ficketticketing.domain.order.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Getter
@Entity
public class RefundPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cancellationPeriod;

    private String refundFeeDescription;

    private int priority;

}