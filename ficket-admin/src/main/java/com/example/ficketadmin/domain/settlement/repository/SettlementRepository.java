package com.example.ficketadmin.domain.settlement.repository;

import com.example.ficketadmin.domain.settlement.entity.Settlement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {

    Optional<Settlement> findByOrderId(Long orderId);
}
