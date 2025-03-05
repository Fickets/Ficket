package com.example.ficketevent.domain.event.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EventStage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long stageId; // 공연장 ID

    @Column(nullable = false)
    private String stageName; // 공연장 이름

    @Column(nullable = false)
    private Integer stageSize; // 공연장 규모

    @Column(nullable = false)
    private String sido; // 시도

    @Column(nullable = false)
    private String sigungu; // 시군구

    @Column(nullable = false)
    private String street; // 상세 주소

    private String eventStageImg;

    @Builder.Default
    @OneToMany(mappedBy = "eventStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>(); // 공연 목록

    @Builder.Default
    @OneToMany(mappedBy = "eventStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StageSeat> stageSeats = new ArrayList<>(); // 좌석 목록

}
