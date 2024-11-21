package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_stage SET deleted_at = CURRENT_TIMESTAMP WHERE stage_id = ?")
public class EventStage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "STAGE_ID")
    private Long stageId; // 공연장 ID

    @Column(name = "STAGE_NAME", nullable = false)
    private String stageName; // 공연장 이름

    @Column(name = "STAGE_SIZE", nullable = false)
    private Integer stageSize; // 공연장 규모

    @Column(name = "SIDO", nullable = false)
    private String sido; // 시도

    @Column(name = "SIGUNGU", nullable = false)
    private String sigungu; // 시군구

    @Column(name = "STREET", nullable = false)
    private String street; // 상세 주소

    @Column(name = "EVENT_STAGE_IMG")
    private String eventStageImg;

    @Builder.Default
    @OneToMany(mappedBy = "eventStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events = new ArrayList<>(); // 공연 목록

    @Builder.Default
    @OneToMany(mappedBy = "eventStage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StageSeat> stageSeats = new ArrayList<>(); // 좌석 목록

    // 연관관계 편의 메서드
    public void addStageSeat(StageSeat stageSeat) {
        stageSeats.add(stageSeat);
        stageSeat.setEventStage(this);
    }

}
