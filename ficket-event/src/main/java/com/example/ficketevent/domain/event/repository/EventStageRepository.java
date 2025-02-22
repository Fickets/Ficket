package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EventStageRepository extends JpaRepository<EventStage, Long> {

    @Query("SELECT DISTINCT e.sido FROM EventStage e")
    List<String> findAllSido();
}
