package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchRes;
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
}
