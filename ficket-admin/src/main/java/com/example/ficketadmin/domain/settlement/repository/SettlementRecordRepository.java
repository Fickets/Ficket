package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.entity.SettlementRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRecordRepository extends JpaRepository<SettlementRecord, Long> {

    Optional<SettlementRecord> findByEventId(Long eventId);
}
