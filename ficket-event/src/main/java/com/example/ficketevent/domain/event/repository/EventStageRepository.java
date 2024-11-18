package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventStage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventStageRepository extends JpaRepository<EventStage, Long> {

//    @Query("SELECT new com.example.ficketevent.domain.event.dto.response.SimpleEventStageDto(e.stageId, e.stageName) " +
//            "FROM EventStage e")
//    List<SimpleEventStageDto> findAllSimpleStages();
}
