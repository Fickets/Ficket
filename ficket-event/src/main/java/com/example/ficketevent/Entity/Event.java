package com.example.ficketevent.Entity;

import com.example.ficketevent.enums.Age;
import com.example.ficketevent.enums.Genre;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.time.LocalDateTime;
import java.util.List;


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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "COMPANY_ID", nullable = false)
    private Company company; // 회사

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "STAGE_ID", nullable = false)
    private EventStage stage; // 공연장

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
    private Integer reservationLimit; // 1인당 예약 제한 인원
}
