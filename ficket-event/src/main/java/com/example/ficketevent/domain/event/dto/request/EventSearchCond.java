package com.example.ficketevent.domain.event.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class EventSearchCond {
    @Schema(description = "이벤트 ID", example = "1")
    private Long eventId;

    @Schema(description = "이벤트 제목", example = "뮤지컬 라이온킹")
    private String eventTitle;

    @Schema(description = "공연 회사 ID", example = "101")
    private Long companyId;

    @Schema(description = "관리자 ID", example = "303")
    private Long adminId;

    @Schema(description = "공연장 ID", example = "202")
    private Long eventStageId;

    @Schema(description = "검색 시작 날짜", example = "2024-12-16T00:00:00")
    private LocalDateTime startDate;

    @Schema(description = "검색 종료 날짜", example = "2024-12-31T00:00:00")
    private LocalDateTime endDate;
}
