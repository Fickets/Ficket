package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {

    void deleteByEvent(Event event);

    @Query("SELECT es.eventDate FROM EventSchedule es WHERE es.eventScheduleId = :eventScheduleId")
    LocalDateTime findEventDateByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT es.event.reservationLimit FROM EventSchedule es WHERE es.eventScheduleId = :eventScheduleId")
    Integer findReservationLimit(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT es.event.eventId FROM EventSchedule es WHERE es.eventScheduleId = :eventScheduleId")
    Long findEventIdByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT es.event FROM EventSchedule es WHERE es.eventScheduleId = :eventScheduleId")
    Optional<Event> findEventByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT es FROM EventSchedule es WHERE es.event = :event")
    List<EventSchedule> findEventScheduleByEvent(@Param("event") Event event);
}
