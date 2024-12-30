package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.request.SelectSeatInfo;
import com.example.ficketevent.domain.event.dto.response.SeatCntByGrade;
import com.example.ficketevent.domain.event.dto.response.SeatGradeInfo;
import com.example.ficketevent.domain.event.dto.response.SeatInfo;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import com.example.ficketevent.domain.event.entity.SeatMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface SeatMappingRepository extends JpaRepository<SeatMapping, Long> {


    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.SeatInfo(sm.seatMappingId, sm.stageSeat.x, sm.stageSeat.y, " +
            "sm.stagePartition.partitionName, sm.stageSeat.seatRow, sm.stageSeat.seatCol, sm.ticketId IS NOT NULL, sm.stagePartition.partitionPrice) " +
            "FROM SeatMapping sm WHERE sm.eventSchedule.eventScheduleId = :eventScheduleId")
    List<SeatInfo> findSeatInfoByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.SeatCntByGrade(sp.partitionName, COUNT(sp.partitionName)) " +
            "FROM StagePartition sp " +
            "JOIN SeatMapping sm ON sp.partitionId = sm.stagePartition.partitionId " +
            "WHERE sm.eventSchedule.eventScheduleId = :eventScheduleId " +
            "AND sm.ticketId IS NULL " +
            "GROUP BY sp.partitionName")
    List<SeatCntByGrade> findPartitionSeatCounts(@Param("eventScheduleId") Long eventScheduleId);


    @Query("SELECT sm FROM SeatMapping sm WHERE sm.ticketId IS NULL AND sm.seatMappingId = :seatMappingId")
    Optional<SeatMapping> findBySeatMappingIdAndTicketIdIsNull(@Param("seatMappingId") Long seatMappingId);

    @Query("SELECT new com.example.ficketevent.domain.event.dto.request.SelectSeatInfo(sm.seatMappingId, sm.stagePartition.partitionPrice, sm.stagePartition.partitionName) " +
            "FROM SeatMapping sm WHERE sm.seatMappingId IN :seatMappingIds")
    Set<SelectSeatInfo> findSeatInfoInSeatMappingIds(@Param("seatMappingIds") Set<Long> seatMappingIds);

    @Modifying
    @Query("UPDATE SeatMapping sm SET sm.ticketId = NULL WHERE sm.ticketId = :ticketId")
    void openSeat(@Param("ticketId") Long ticketId);

    @Query("SELECT COUNT(sm.ticketId) FROM SeatMapping sm WHERE sm.ticketId in :ticketIds")
    int countPurchasedSeatsByTicketId(@Param("ticketIds") List<Long> ticketIds);

    @Query("SELECT COUNT(sm.ticketId) FROM SeatMapping sm WHERE sm.ticketId = :ticketId")
    int countSeatMappingByTicketId(@Param("ticketId") Long ticketId);

    Optional<SeatMapping> findByTicketId(Long ticketId);

    List<SeatMapping> findAllByTicketId(Long ticketId);

    void deleteSeatMappingByEventSchedule(EventSchedule eventSchedule);

    void deleteAllByEventScheduleIn(List<EventSchedule> eventSchedules);


}