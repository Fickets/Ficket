package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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
    @Column(nullable = false)
    private Long eventScheduleId; // 행사 날짜 ID

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event; // 이벤트 ID 참조

    @Column(nullable = false)
    private Integer round; // 회차

    @Column(nullable = false, columnDefinition = "TIMESTAMP")
    private LocalDateTime eventDate; // 행사 날짜 및 시간

    @Builder.Default
    @OneToMany(mappedBy = "eventSchedule", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatMapping> seatMappingList = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addSeatMapping(SeatMapping seatMapping) {
        seatMappingList.add(seatMapping);
        seatMapping.setEventSchedule(this);
    }
}
