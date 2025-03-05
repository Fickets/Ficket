package com.example.ficketticketing.domain.order.repository;

import com.example.ficketticketing.domain.order.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByEventScheduleId(Long eventId);

    @Modifying
    @Query("UPDATE Ticket t SET t.viewingStatus = 'DELETED' WHERE t.ticketId = :ticketId")
    void deleteByTicketId(@Param("ticketId") Long ticketId);
}
