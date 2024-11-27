package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Getter
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE event_image SET deleted_at = CURRENT_TIMESTAMP WHERE event_img_id = ?")
public class EventImage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long eventImgId; // 이미지 ID

    private String posterOriginUrl; // 원본 포스터 이미지 URL

    private String posterMobileUrl; // 모바일 포스터 이미지 URL

    private String posterPcUrl; // PC 포스터 이미지 URL

    private String posterPcMain1Url; // PC 포스터 메인1 이미지 URL

    private String posterPcMain2Url; // PC 포스터 메인2 이미지 URL

    private String bannerOriginUrl; // 원본 배너 이미지 URL

    private String bannerPcUrl; // PC 배너 이미지 URL

    private String bannerMobileUrl; // 모바일 배너 이미지 URL

    @Setter
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evend_id", nullable = false)
    private Event event; // 관련 이벤트


    public void updateBanner(String bannerOriginUrl, String bannerPcUrl, String bannerMobileUrl) {
        this.bannerOriginUrl = bannerOriginUrl;
        this.bannerPcUrl = bannerPcUrl;
        this.bannerMobileUrl = bannerMobileUrl;
    }

    public void updatePoster(String posterOriginUrl, String posterMobileUrl, String posterPcUrl, String posterPcMain1Url, String posterPcMain2Url) {
        this.posterOriginUrl = posterOriginUrl;
        this.posterMobileUrl = posterMobileUrl;
        this.posterPcUrl = posterPcUrl;
        this.posterPcMain1Url = posterPcMain1Url;
        this.posterPcMain2Url = posterPcMain2Url;
    }

}
