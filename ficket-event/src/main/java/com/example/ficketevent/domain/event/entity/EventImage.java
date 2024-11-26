package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_image SET deleted_at = CURRENT_TIMESTAMP WHERE event_img_id = ?")
public class EventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EVENT_IMG_ID")
    private Long eventImgId; // 이미지 ID

    @Column(name = "POSTER_ORIGIN_URL", nullable = false)
    private String posterOriginUrl; // 원본 포스터 이미지 URL

    @Column(name = "POSTER_MOBILE_URL")
    private String posterMobileUrl; // 모바일 포스터 이미지 URL

    @Column(name = "POSTER_PC_URL")
    private String posterPcUrl; // PC 포스터 이미지 URL

    @Column(name = "POSTER_PC_MAIN1_URL")
    private String posterPcMain1Url; // PC 포스터 메인1 이미지 URL

    @Column(name = "POSTER_PC_MAIN2_URL")
    private String posterPcMain2Url; // PC 포스터 메인2 이미지 URL

    @Column(name = "BANNER_ORIGIN_URL", nullable = false)
    private String bannerOriginUrl; // 원본 배너 이미지 URL

    @Column(name = "BANNER_PC_URL")
    private String bannerPcUrl; // PC 배너 이미지 URL

    @Column(name = "BANNER_MOBILE_URL")
    private String bannerMobileUrl; // 모바일 배너 이미지 URL

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EVENT_ID", nullable = false)
    private Event event; // 관련 이벤트

}
