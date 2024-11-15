package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventStageRepository extends JpaRepository<EventStage, Long> {
}
