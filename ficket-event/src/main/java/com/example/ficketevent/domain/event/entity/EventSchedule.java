package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;


@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_schedule SET deleted_at = CURRENT_TIMESTAMP WHERE event_schedule_id = ?")
public class EventSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_SCHEDULE_ID", nullable = false)
    private Long eventScheduleId; // 행사 날짜 ID

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event; // 이벤트 ID 참조

    @Column(name = "Round", nullable = false)
    private Integer round; // 회차

    @Column(name = "Event_Date", nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventDate; // 행사 날짜 및 시간

}
