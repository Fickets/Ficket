package com.example.ficketevent.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StageSeat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seatId; // 좌석 ID

    @Column(nullable = false)
    private Double x; // 좌표 x

    @Column(nullable = false)
    private Double y; // 좌표 y

    @Column(nullable = false)
    private String seatCol; // 열번호

    @Column(nullable = false)
    private String seatRow; // 행번호

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stage_id", nullable = false)
    private EventStage eventStage; // 공연장과의 관계

}
