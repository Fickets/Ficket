package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventScheduleRepository extends JpaRepository<EventSchedule, Long> {
}
