package com.example.ficketticketing.domain.order.repository;

import com.example.ficketticketing.domain.order.entity.Ticket;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TicketRepository extends JpaRepository<Ticket, Long> {

    List<Ticket> findAllByEventScheduleId(Long eventId);
}
