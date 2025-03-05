package com.example.ficketevent.domain.event.repository;

import com.example.ficketevent.domain.event.entity.FailedItem;
import com.example.ficketevent.domain.event.enums.JobStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FailedItemRepository extends JpaRepository<FailedItem, Long> {

    List<FailedItem> findByStatus(JobStatus status);

    List<FailedItem> findAllByItemIdIn(List<Long> itemIds);
}