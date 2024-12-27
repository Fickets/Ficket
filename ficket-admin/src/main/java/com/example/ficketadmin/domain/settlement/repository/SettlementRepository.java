package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByOrderId(Long orderId);
}
