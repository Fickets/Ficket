package com.example.ficketsearch.domain.search.enums;

import co.elastic.clients.elasticsearch._types.SortOrder;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum SortBy {
    SORT_BY_ACCURACY("_score", SortOrder.Desc, null),
    SORT_BY_PERFORMANCE_IMMINENT("Schedules.Schedule", SortOrder.Asc, "Schedules");

    private final String field;
    private final SortOrder order;
    private final String nestedPath;
}