package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.common.EventTitleDto;
import com.example.ficketevent.domain.event.dto.common.TicketInfoDto;
import com.example.ficketevent.domain.event.dto.response.TicketEventResponse;
import com.example.ficketevent.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventCustomRepository {

    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.TicketEventResponse(sm.ticketId, es.eventDate, e.eventStage.stageName, ei.bannerPcUrl, ei.bannerMobileUrl, e.title, e.companyId, sp.partitionName, ss.seatRow, ss.seatCol, est.sido) " +
            "FROM SeatMapping sm " +
            "JOIN EventSchedule es ON sm.eventSchedule.eventScheduleId = es.eventScheduleId " +
            "JOIN Event e ON es.event.eventId = e.eventId " +
            "JOIN EventImage ei ON e.eventId = ei.event.eventId " +
            "JOIN StagePartition sp ON sm.stagePartition.partitionId = sp.partitionId " +
            "JOIN EventStage est ON e.eventStage.stageId = est.stageId " +
            "JOIN StageSeat ss ON sm.stageSeat.seatId = ss.seatId " +
            "WHERE sm.ticketId in :ticketIds ")
    List<TicketEventResponse> getMyTicketInfo(@Param("ticketIds") List<Long> ticketIds);

    @Query("SELECT e.eventId FROM Event e WHERE e.ticketingTime BETWEEN :startOfDay AND :endOfDay")
    List<Long> findOpenEvents(@Param("startOfDay") LocalDateTime startOfDay, @Param("endOfDay") LocalDateTime endOfDay);

    @Query("SELECT new com.example.ficketevent.domain.event.dto.common.EventTitleDto(e.eventId, e.companyId, e.title) FROM Event e WHERE e.title LIKE %:title%")
    List<EventTitleDto> findEventIds(@Param("title")String title);

    @Query("SELECT e.title FROM Event e")
    List<String> findEventTitle();
}
