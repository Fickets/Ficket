package com.example.ficketsearch.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class IndexingKafkaMessage {
    private Object payload;
    private String operationType; // "CREATE", "UPDATE", "DELETE"
}