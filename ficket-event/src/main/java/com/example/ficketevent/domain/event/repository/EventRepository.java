package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.common.EventTitleDto;
import com.example.ficketevent.domain.event.dto.response.EventSummaryProjection;
import com.example.ficketevent.domain.event.dto.response.TicketEventResponse;
import com.example.ficketevent.domain.event.entity.Event;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

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
    List<EventTitleDto> findEventIds(@Param("title") String title);

    @Query("SELECT e.title FROM Event e")
    List<String> findEventTitle();

    // spring batch
    @Query("SELECT e.eventId FROM Event e WHERE e.eventId BETWEEN :startId AND :endId")
    Page<Event> findEventIdsByRange(@Param("startId") Long startId, @Param("endId") Long endId, Pageable pageable);

    @Query("SELECT MIN(e.eventId) FROM Event e")
    Long findMinId();

    @Query("SELECT MAX(e.eventId) FROM Event e")
    Long findMaxId();

    @Query(
            value = """
        SELECT
            e.event_id AS eventId,
            e.title AS title,
            es.stage_name AS stageName,
            e.company_id AS companyId,
            e.admin_id AS adminId,
            esc.min_event_date AS minEventDate,
            esc.max_event_date AS maxEventDate
        FROM
            event e
        JOIN
            event_stage es ON e.stage_id = es.stage_id
        JOIN (
            SELECT
                event_id,
                MIN(event_date) AS min_event_date,
                MAX(event_date) AS max_event_date
            FROM
                event_schedule
            WHERE
                deleted_at IS NULL
            GROUP BY
                event_id
            LIMIT :limit OFFSET :offset
        ) esc ON e.event_id = esc.event_id
        WHERE
            e.deleted_at IS NULL
            AND (:eventId IS NULL OR e.event_id = :eventId)
            AND (:eventTitle IS NULL OR e.title LIKE CONCAT('%', :eventTitle, '%'))
            AND (:companyId IS NULL OR e.company_id = :companyId)
            AND (:adminId IS NULL OR e.admin_id = :adminId)
            AND (:eventStageId IS NULL OR e.stage_id = :eventStageId)
            AND (:startDate IS NULL OR esc.min_event_date >= :startDate)
            AND (:endDate IS NULL OR esc.max_event_date <= :endDate)
        """,
            nativeQuery = true)
    List<EventSummaryProjection> searchEventByCond(
            @Param("eventId") Long eventId,
            @Param("eventTitle") String eventTitle,
            @Param("companyId") Long companyId,
            @Param("adminId") Long adminId,
            @Param("eventStageId") Long eventStageId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("limit") int limit,
            @Param("offset") long offset
    );

    @Query(value = """
    SELECT COUNT(DISTINCT e.event_id)
    FROM event e
    JOIN event_stage es ON e.stage_id = es.stage_id
    JOIN event_schedule esc ON e.event_id = esc.event_id AND esc.deleted_at IS NULL
    WHERE e.deleted_at IS NULL
      AND (:eventId IS NULL OR e.event_id = :eventId)
      AND (:eventTitle IS NULL OR e.title LIKE CONCAT('%', :eventTitle, '%'))
      AND (:companyId IS NULL OR e.company_id = :companyId)
      AND (:adminId IS NULL OR e.admin_id = :adminId)
      AND (:eventStageId IS NULL OR e.stage_id = :eventStageId)
      AND (:startDate IS NULL OR esc.event_date >= :startDate)
      AND (:endDate IS NULL OR esc.event_date <= :endDate)
    """, nativeQuery = true)
    long countEventsByCond(
            @Param("eventId") Long eventId,
            @Param("eventTitle") String eventTitle,
            @Param("companyId") Long companyId,
            @Param("adminId") Long adminId,
            @Param("eventStageId") Long eventStageId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    @Query(value = "SELECT " +
            "e.event_id AS eventId, " +
            "e.title AS title, " +
            "es.stage_name AS stageName, " +
            "es.sido AS sido, " +
            "ANY_VALUE(ei.poster_pc_url) AS posterUrl, " +
            "e.ticketing_time AS ticketingTime, " +
            "GROUP_CONCAT(DISTINCT g.genre) AS genreList, " +
            "GROUP_CONCAT(DISTINCT ec.event_date) AS eventDateList " +
            "FROM event e " +
            "JOIN event_stage es ON e.stage_id = es.stage_id " +
            "LEFT JOIN event_image ei ON e.event_id = ei.event_id " +
            "JOIN event_schedule ec ON e.event_id = ec.event_id " +
            "JOIN event_genre g ON e.event_id = g.event_id " +
            "WHERE e.event_id IN :eventIds " +
            "GROUP BY e.event_id",
            nativeQuery = true)
    List<Map<String, Object>> findEventIndexingInfoRawBulk(@Param("eventIds") List<Long> eventIds);

}
