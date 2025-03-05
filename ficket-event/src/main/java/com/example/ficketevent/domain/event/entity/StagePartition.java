package com.example.ficketevent.domain.event.entity;

import com.example.ficketevent.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE stage_partition SET deleted_at = CURRENT_TIMESTAMP WHERE partition_id = ?")
public class StagePartition extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long partitionId;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event; // Reference to Event entity

    @Column(nullable = false)
    private String partitionName;

    @Column(nullable = false)
    private BigDecimal partitionPrice;

    @Builder.Default
    @OneToMany(mappedBy = "stagePartition", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SeatMapping> seatMappings = new ArrayList<>();

    // 연관 관계 편의 메서드
    public void addSeatMapping(SeatMapping seatMapping) {
        seatMappings.add(seatMapping);
        seatMapping.setStagePartition(this);
    }

    public void setSeatMappings(List<SeatMapping> seatMappings) {
        this.seatMappings = seatMappings;
        seatMappings.forEach(seatMapping -> seatMapping.setStagePartition(this));
    }
}