package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.StageSeat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StageSeatRepository extends JpaRepository<StageSeat, Long> {
}
