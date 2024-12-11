package com.example.ficketticketing.domain.order.repository;

import com.example.ficketticketing.domain.order.entity.RefundPolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RefundPolicyRepository extends JpaRepository<RefundPolicy, Long> {

    @Query("SELECT rp FROM RefundPolicy rp ORDER BY rp.priority")
    List<RefundPolicy> findAllOrderByPriority();

}
