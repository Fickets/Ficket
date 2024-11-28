package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.dto.response.SeatCntByGrade;
import com.example.ficketevent.domain.event.dto.response.SeatGradeInfo;
import com.example.ficketevent.domain.event.dto.response.SeatInfo;
import com.example.ficketevent.domain.event.entity.SeatMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatMappingRepository extends JpaRepository<SeatMapping, Long> {


    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.SeatInfo(sm.seatMappingId, sm.stageSeat.x, sm.stageSeat.y, " +
            "sm.stagePartition.partitionName, sm.stageSeat.seatRow, sm.stageSeat.seatCol, sm.ticketId IS NOT NULL) " +
            "FROM SeatMapping sm WHERE sm.eventSchedule.eventScheduleId = :eventScheduleId")
    List<SeatInfo> findSeatInfoByEventScheduleId(@Param("eventScheduleId") Long eventScheduleId);

    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.SeatCntByGrade(sp.partitionName, COUNT(sp.partitionName)) " +
            "FROM StagePartition sp " +
            "JOIN SeatMapping sm ON sp.partitionId = sm.stagePartition.partitionId " +
            "WHERE sm.eventSchedule.eventScheduleId = :eventScheduleId " +
            "AND sm.ticketId IS NULL " +
            "GROUP BY sp.partitionName")
    List<SeatCntByGrade> findPartitionSeatCounts(@Param("eventScheduleId") Long eventScheduleId);



}
