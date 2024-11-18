package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import com.example.ficketevent.domain.event.entity.StageSeat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StageSeatRepository extends JpaRepository<StageSeat, Long> {

    List<StageSeat> findByEventStage(EventStage stage);
}
