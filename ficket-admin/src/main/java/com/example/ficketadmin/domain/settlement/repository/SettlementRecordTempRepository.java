package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.entity.SettlementRecord;
import com.example.ficketadmin.domain.settlement.entity.SettlementRecordTemp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRecordTempRepository extends JpaRepository<SettlementRecordTemp, Long> {

    Optional<SettlementRecordTemp> findBySettlementRecordId(Long id);

    Optional<SettlementRecordTemp> findByEventId(Long eventId);

}
