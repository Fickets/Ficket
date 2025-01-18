package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.domain.event.enums.JobStatus;
import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FailedItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long itemId;

    @Column(nullable = false)
    private String reason;

    @Setter
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private JobStatus status; // PENDING, RETRIED, SUCCESS

}
