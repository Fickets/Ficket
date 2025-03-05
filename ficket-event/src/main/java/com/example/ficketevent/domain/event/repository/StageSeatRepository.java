package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import com.example.ficketevent.domain.event.entity.StageSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StageSeatRepository extends JpaRepository<StageSeat, Long> {

    List<StageSeat> findByEventStage(EventStage stage);

    @Query("SELECT COALESCE(SUM(es.stageSize), 0) FROM Event e " +
            "JOIN EventStage es ON e.eventStage = es " +
            "JOIN EventSchedule esc ON e.eventId = esc.event.eventId " +
            "WHERE e.ticketingTime <= :end AND esc.eventDate >= :start")
    BigDecimal findTotalSeatsForPeriod(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

}
