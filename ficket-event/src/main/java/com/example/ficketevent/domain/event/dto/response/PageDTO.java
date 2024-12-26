package com.example.ficketevent.domain.event.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;

@Data
@AllArgsConstructor
public class PageDTO<T> {
    private List<T> content;
    private int pageNumber;
    private int pageSize;
    private long totalElements;
    private long totalPages;
    private String sortProperty; // 정렬 기준 필드
    private String sortDirection; // 정렬 방향 (ASC, DESC)

    public static <T> PageDTO<T> toPageDTO(Page<T> page) {
        String sortProperty = "unsorted";
        String sortDirection = "none";

        // 정렬 정보가 있는 경우 처리
        if (page.getSort().isSorted()) {
            Sort.Order order = page.getSort().iterator().next(); // 첫 번째 정렬 기준만 사용
            sortProperty = order.getProperty();
            sortDirection = order.getDirection().name();
        }

        return new PageDTO<>(
                page.getContent(), // 컨텐츠 리스트
                page.getNumber(),  // 현재 페이지 번호
                page.getSize(),    // 페이지 크기
                page.getTotalElements(), // 전체 데이터 개수
                page.getTotalPages(),
                sortProperty, // 정렬 기준 필드
                sortDirection // 정렬 방향
        );
    }
}
