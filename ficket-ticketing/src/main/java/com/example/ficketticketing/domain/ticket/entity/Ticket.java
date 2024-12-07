package com.example.ficketticketing.domain.ticket.entity;

import com.example.ficketticketing.domain.order.entity.Orders;
import com.example.ficketticketing.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE ticket SET deleted_at = CURRENT_TIMESTAMP WHERE ticket_id = ?")
public class Ticket extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ticketId;

    @Enumerated(EnumType.STRING)
    private ViewingStatus viewingStatus;

    private Long eventScheduleId;

    @Setter
    @OneToOne(mappedBy = "ticket")
    private Orders orders;
}
