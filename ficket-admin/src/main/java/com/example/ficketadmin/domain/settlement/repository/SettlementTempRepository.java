package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import com.example.ficketadmin.domain.settlement.entity.SettlementTemp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementTempRepository extends JpaRepository<SettlementTemp, Long> {


    @Query("SELECT s FROM SettlementTemp s WHERE s.settlementRecord.eventId = :eventId AND s.isSettled = false")
    List<SettlementTemp> findSettlementByEventId(@Param("eventId") Long eventId);

    Optional<SettlementTemp> findByOrderId(Long orderId);
}
