package com.example.ficketticketing.domain.order.repository;

import com.example.ficketticketing.domain.order.dto.client.DailyRevenueResponse;
import com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse;
import com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDto;
import com.example.ficketticketing.domain.order.entity.OrderStatus;
import com.example.ficketticketing.domain.order.entity.Orders;
import com.example.ficketticketing.domain.order.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface OrderRepository extends JpaRepository<Orders, Long> {

    @Query("SELECT o.orderPrice FROM Orders o WHERE o.paymentId = :paymentId")
    Optional<BigDecimal> findOrderByPaymentId(@Param("paymentId") String paymentId);

    @Modifying
    @Query("UPDATE Orders o SET o.orderStatus = 'COMPLETED' WHERE o.paymentId = :paymentId AND o.orderStatus = 'INPROGRESS'")
    void updateOrderStatusToCompleted(@Param("paymentId") String paymentId);

    @Modifying
    @Query("UPDATE Orders o SET o.orderStatus = 'CANCELLED' WHERE o.paymentId = :paymentId AND o.orderStatus = 'INPROGRESS'")
    void cancelByPaymentId(@Param("paymentId") String paymentId);

//    @Modifying
//    @Query("UPDATE Orders o SET o.orderStatus = :orderStatus WHERE o.orderId = :orderId")
//    void updateOrderStatus(@Param("orderId") Long orderId, @Param("orderStatus") OrderStatus orderStatus);

    @Query("SELECT new com.example.ficketticketing.domain.order.dto.response.OrderStatusResponse(o.orderStatus) FROM Orders o WHERE o.orderId = :orderId")
    Optional<OrderStatusResponse> findOrderStatusByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT o.ticket.ticketId FROM Orders o WHERE o.paymentId = :paymentId")
    Long findTicketIdByPaymentId(@Param("paymentId") String paymentId);

    @Query("SELECT new com.example.ficketticketing.domain.order.dto.response.TicketInfoCreateDto(o.orderId, o.ticket.ticketId, o.createdAt) FROM Orders o WHERE o.userId = :userId AND o.orderStatus = 'COMPLETED'")
    List<TicketInfoCreateDto> findTicketIdsByUserId(@Param("userId") Long userId);

    Optional<Orders> findByTicket(Ticket ticket);

    @Query("SELECT o.ticket.ticketId " +
            "FROM Orders o " +
            "WHERE o.userId = :userId " +
            "AND o.orderStatus = 'COMPLETED' " +
            "AND o.ticket.eventScheduleId = :eventScheduleId")
    List<Long> findTicketIdByEventSchedule(@Param("userId") Long userId, @Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT o FROM Orders o WHERE o.orderId = :orderId AND o.orderStatus = 'COMPLETED'")
    Optional<Orders> findByIdCompletedStatus(@Param("orderId") Long orderId);

    @Query("SELECT new com.example.ficketticketing.domain.order.dto.client.DailyRevenueResponse(DATE(o.createdAt), SUM(o.orderPrice - o.refundPrice))" +
            "FROM Orders o " +
            "WHERE o.ticket.ticketId IN :ticketIds " +
            "AND (o.orderStatus = 'COMPLETED' OR o.orderStatus = 'REFUNDED') " +
            "GROUP BY DATE(o.createdAt)")
    List<DailyRevenueResponse> calculateDailyRevenue(@Param("ticketIds") Set<Long> ticketIds);

    @Query("SELECT DAYNAME(o.createdAt), COUNT(o) " +
            "FROM Orders o " +
            "WHERE o.ticket.ticketId IN :ticketIds " +
            "AND o.orderStatus = 'COMPLETED' " +
            "AND o.createdAt BETWEEN :startOfWeek AND :endOfWeek " +
            "GROUP BY DAYNAME(o.createdAt)")
    List<Object[]> calculateDayCount(@Param("ticketIds") Set<Long> ticketIds,
                                     @Param("startOfWeek") LocalDateTime startOfWeek,
                                     @Param("endOfWeek") LocalDateTime endOfWeek);


    @Query("SELECT o " +
            "FROM Orders o JOIN o.ticket t " +
            "WHERE o.orderStatus = com.example.ficketticketing.domain.order.entity.OrderStatus.COMPLETED " +
            "AND t.viewingStatus = com.example.ficketticketing.domain.order.entity.ViewingStatus.NOT_WATCHED " +
            "AND o.userId = :userId")
    List<Orders> findAllByUserId(@Param("userId") Long userId);

    @Query("SELECT o.userId FROM Orders o WHERE o.paymentId = :paymentId")
    Long findUserIdByPaymentId(@Param("paymentId") String paymentId);

//    @Query("SELECT o.userId FROM Orders o WHERE o.orderId = :orderId")
//    Long findUserIdByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT o From Orders o WHERE o.orderId = :orderId")
    Optional<Orders> findByOrderId(@Param("orderId") Long orderId);

    @Query("SELECT o FROM Orders o WHERE o.paymentId = :paymentId")
    Optional<Orders> findByPaymentId(@Param("paymentId") String paymentId);

}

