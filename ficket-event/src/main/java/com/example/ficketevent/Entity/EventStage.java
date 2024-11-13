package com.example.ficketevent.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.util.List;

@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_stage SET deleted_at = CURRENT_TIMESTAMP WHERE event_stage_id = ?")
public class EventStage extends BaseEntity{

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

    @OneToMany(mappedBy = "stage", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Event> events; // 공연 목록

}
