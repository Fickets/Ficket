package com.example.ficketevent.Entity;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

/**
 * 이벤트 이미지를 저장하는 엔티티 클래스
 */
@Entity
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_image SET deleted_at = CURRENT_TIMESTAMP WHERE event_image_id = ?")
public class EventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_IMG_ID")
    private Long eventImgId; // 이미지 ID

    @Column(name = "POSTER", nullable = false)
    private String poster; // 포스터 이미지 URL

    @Column(name = "BANNER", nullable = false)
    private String banner; // 배너 이미지 URL

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event; // 관련 이벤트

}
