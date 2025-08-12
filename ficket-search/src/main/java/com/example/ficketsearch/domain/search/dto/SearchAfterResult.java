package com.example.ficketsearch.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class SearchAfterResult {
    private Long totalSize;
    private List<Event> events;
    private List<Object> nextSearchAfter;
}