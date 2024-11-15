package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventRepository extends JpaRepository<Event, Long> {
}
