package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import com.example.ficketevent.domain.event.enums.Age;
import com.example.ficketevent.domain.event.enums.Genre;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event SET deleted_at = CURRENT_TIMESTAMP WHERE event_id = ?")
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_ID", nullable = false)
    private Long eventId; // 이벤트 ID

    @Column(name = "ADMIN_ID", nullable = false)
    private Long adminId; // 관리자 ID

    @Column(name = "COMPANY_ID", nullable = false)
    private Long companyId; // 회사 ID

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_ID", nullable = false)
    private EventStage eventStage; // 공연장

    @ElementCollection(targetClass = Genre.class)
    @CollectionTable(name = "event_genre", joinColumns = @JoinColumn(name = "EVENT_ID"))
    @Enumerated(EnumType.STRING)
    @Column(name = "GENRE", nullable = false)
    private List<Genre> genre; // 장르 목록

    @Enumerated(EnumType.STRING)
    @Column(name = "AGE", nullable = false)
    private Age age; // 관람 연령

    @Column(name = "CONTENT", nullable = false, columnDefinition = "TEXT")
    private String content; // 공연 상세 내용

    @Column(name = "TITLE", nullable = false, length = 100)
    private String title; // 제목

    @Column(name = "SUBTITLE", nullable = false, length = 100)
    private String subTitle; // 부제목

    @Column(name = "TICKETING_TIME", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime ticketingTime; // 티켓팅 시작 시간

    @Column(name = "RUNNING_TIME", nullable = false)
    private Integer runningTime; // 상영 시간

    @Column(name = "RESERVATION_LIMIT", nullable = false)
    private Integer reservationLimit; // 1인당 티켓 매수 제한

    @OneToOne(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private EventImage eventImage;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<EventSchedule> eventSchedules = new ArrayList<>(); // 행사 날짜 관리

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<StagePartition> stagePartitions = new ArrayList<>(); // 행사 좌석 구분

    // 연관 관계 설정 메서드
    public void addEventImage(EventImage eventImage) {
        this.eventImage = eventImage;
        eventImage.setEvent(this);
    }

    public void addEventSchedule(EventSchedule eventSchedule) {
        eventSchedules.add(eventSchedule);
        eventSchedule.setEvent(this);
    }

    public void addStagePartition(StagePartition stagePartition) {
        stagePartitions.add(stagePartition);
        stagePartition.setEvent(this);
    }

    public void addEventStage(EventStage eventStage) {
        this.eventStage = eventStage;
        eventStage.getEvents().add(this);
    }

}
