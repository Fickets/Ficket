package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageSeat extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "SEAT_ID")
    private Long seatId; // 좌석 ID

    @Column(name = "x", nullable = false)
    private Double x; // 좌표 x

    @Column(name = "y", nullable = false)
    private Double y; // 좌표 y

    @Column(name = "seat_col", nullable = false)
    private String seatCol; // 열번호

    @Column(name = "seat_row", nullable = false)
    private String seatRow; // 행번호

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_ID", nullable = false)
    private EventStage eventStage; // 공연장과의 관계

}
