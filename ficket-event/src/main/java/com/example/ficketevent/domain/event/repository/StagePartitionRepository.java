package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.StagePartition;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StagePartitionRepository extends JpaRepository<StagePartition, Long> {
}
