package com.example.ficketticketing.domain.order.entity;

import com.example.ficketticketing.domain.order.dto.request.CreateOrderRequest;
import com.example.ficketticketing.domain.order.dto.request.SelectSeatInfo;
import com.example.ficketticketing.global.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import java.math.BigDecimal;

@Builder
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SQLRestriction("deleted_at IS NULL")
@SQLDelete(sql = "UPDATE orders SET deleted_at = CURRENT_TIMESTAMP WHERE orders_id = ?")
public class Orders extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long orderId;

    private String paymentId;

    private BigDecimal orderPrice;

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus;

    private BigDecimal refundPrice;

    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ticket_id", nullable = false) // Orders가 외래 키를 소유
    private Ticket ticket;

    private Long userId;

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
        ticket.setOrders(this);
    }

    public static Orders createOrder(CreateOrderRequest createOrderRequest, Long userId) {
        Ticket ticket = Ticket.builder()
                .eventScheduleId(createOrderRequest.getEventScheduleId())
                .viewingStatus(ViewingStatus.NOT_WATCHED)
                .build();

        Orders order = Orders.builder()
                .paymentId(createOrderRequest.getPaymentId())
                .orderStatus(OrderStatus.INPROGRESS)
                .refundPrice(BigDecimal.ZERO)
                .orderPrice(createOrderRequest.getSelectSeatInfoList().stream()
                        .map(SelectSeatInfo::getSeatPrice)
                        .reduce(BigDecimal.ZERO, BigDecimal::add))
                .userId(userId)
                .build();

        // 관계 설정
        order.setTicket(ticket);

        return order;
    }

    public void refund(BigDecimal refundPrice) {
        this.refundPrice = refundPrice;
        this.orderStatus = OrderStatus.REFUNDED;
    }
}
