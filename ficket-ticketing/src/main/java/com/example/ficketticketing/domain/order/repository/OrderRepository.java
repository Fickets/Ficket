package com.example.ficketticketing.domain.order.repository;

import com.example.ficketticketing.domain.order.entity.OrderStatus;
import com.example.ficketticketing.domain.order.entity.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Orders,Long> {

    @Query("SELECT o.orderPrice FROM Orders o WHERE o.paymentId = :paymentId")
    Optional<BigDecimal> findOrderByPaymentId(@Param("paymentId") String paymentId);

    @Modifying
    @Query("UPDATE Orders o SET o.orderStatus = 'COMPLETED' WHERE o.paymentId = :paymentId AND o.orderStatus = 'INPROGRESS'")
    void updateOrderStatusToCompleted(@Param("paymentId") String paymentId);

    @Modifying
    @Query("UPDATE Orders o SET o.orderStatus = :orderStatus WHERE o.orderId = :orderId")
    void updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") OrderStatus orderStatus);

    @Query("SELECT o.orderStatus FROM Orders o WHERE o.orderId = :orderId")
    Optional<OrderStatus> findOrderStatusByOrderId(@Param("orderId") Long orderId);
}
