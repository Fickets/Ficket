package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.EventImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EventImageRepository extends JpaRepository<EventImage, Long> {
}
