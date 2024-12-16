package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import com.example.ficketevent.domain.event.entity.StageSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface StageSeatRepository extends JpaRepository<StageSeat, Long> {

    List<StageSeat> findByEventStage(EventStage stage);

    // 오늘 기준으로 예매 가능한 행사 전체 좌석 수
    @Query("SELECT COALESCE(SUM(es.stageSize), 0) FROM Event e " +
            "JOIN EventStage es ON e.eventStage = es " +
            "JOIN EventSchedule esc ON e.eventId = esc.event.eventId " +
            "WHERE e.ticketingTime <= CURRENT_DATE AND esc.eventDate >= CURRENT_DATE")
    BigDecimal findTotalSeatsForToday();

    @Query("SELECT COALESCE(SUM(es.stageSize), 0) " +
            "FROM Event e " +
            "JOIN EventStage es ON e.eventStage = es " +
            "JOIN EventSchedule esc ON e.eventId = esc.event.eventId " +
            "WHERE e.ticketingTime <= :yesterday AND esc.eventDate >= :yesterday")
    BigDecimal findTotalSeatsForPreviousDay(@Param("yesterday") LocalDate yesterday);

    @Query("SELECT COALESCE(SUM(es.stageSize), 0) " +
            "FROM Event e " +
            "JOIN EventStage es ON e.eventStage = es " +
            "JOIN EventSchedule esc ON e.eventId = esc.event.eventId " +
            "WHERE e.ticketingTime <= :endOfWeek AND esc.eventDate >= :startOfWeek")
    BigDecimal findTotalSeatsForWeek(@Param("startOfWeek") LocalDate startOfWeek, @Param("endOfWeek") LocalDate endOfWeek);

    @Query("SELECT COALESCE(SUM(es.stageSize), 0) " +
            "FROM Event e " +
            "JOIN EventStage es ON e.eventStage = es " +
            "JOIN EventSchedule esc ON e.eventId = esc.event.eventId " +
            "WHERE e.ticketingTime <= :endOfMonth AND esc.eventDate >= :startOfMonth")
    BigDecimal findTotalSeatsForMonth(@Param("startOfMonth") LocalDate startOfMonth, @Param("endOfMonth") LocalDate endOfMonth);
}
