package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.common.TicketInfoDto;
import com.example.ficketevent.domain.event.dto.response.TicketEventResponse;
import com.example.ficketevent.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventRepository extends JpaRepository<Event, Long>, EventCustomRepository {

//    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.EventSearchRes(" +
//            "e.eventId, e.title, e.eventStage.stageName, null, e.adminId, null, es.eventDate) " +
//            "FROM Event e " +
//            "JOIN EventSchedule es ON e = es.event " +
//            "WHERE (:#{#cond.eventId} IS NULL OR e.eventId = :#{#cond.eventId}) " +
//            "AND (:#{#cond.eventTitle} IS NULL OR e.title LIKE %:#{#cond.eventTitle}%) " +
//            "AND (:#{#cond.companyId} IS NULL OR e.companyId = :#{#cond.companyId}) " +
//            "AND (:#{#cond.adminId} IS NULL OR e.adminId = :#{#cond.adminId}) " +
//            "AND (:#{#cond.eventStageId} IS NULL OR e.eventStage.stageId = :#{#cond.eventStageId}) ")
//    List<EventSearchRes> searchEventByCond(@Param("cond") EventSearchCond eventSearchCond);


    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.TicketEventResponse(es.eventDate, e.eventStage.stageName, ei.bannerPcUrl, ei.bannerMobileUrl, e.title, e.companyId, sp.partitionName, ss.seatRow, ss.seatCol) " +
            "FROM SeatMapping sm " +
            "JOIN EventSchedule es ON sm.eventSchedule.eventScheduleId = es.eventScheduleId " +
            "JOIN Event e ON es.event.eventId = e.eventId " +
            "JOIN EventImage ei ON e.eventId = ei.event.eventId " +
            "JOIN StagePartition sp ON sm.stagePartition.partitionId = sp.partitionId " +
            "JOIN EventStage est ON e.eventStage.stageId = est.stageId " +
            "JOIN StageSeat ss ON sm.stageSeat.seatId = ss.seatId " +
            "WHERE sm.ticketId in :ticketIds ")
    List<TicketEventResponse> getMyTicketInfo(@Param("ticketIds") List<Long> ticketIds);


}
