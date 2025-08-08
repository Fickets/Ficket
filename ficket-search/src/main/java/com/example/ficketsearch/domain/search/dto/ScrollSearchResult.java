package com.example.ficketsearch.domain.search.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class ScrollSearchResult {
    private Long totalCount;       // 전체 결과 수 (첫 요청 시 제공)
    private List<Event> events;    // 이벤트 리스트
    private String scrollId;       // 다음 스크롤 조회에 사용할 scrollId
}