package com.example.ficketsearch.domain.search.repository;

import com.example.ficketsearch.domain.search.entity.PartialIndexing;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;

public interface IndexingRepository extends ReactiveMongoRepository<PartialIndexing, String> {
}
