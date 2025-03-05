package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.entity.StagePartition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StagePartitionRepository extends JpaRepository<StagePartition, Long> {

    void deleteByEvent(Event event);
}
