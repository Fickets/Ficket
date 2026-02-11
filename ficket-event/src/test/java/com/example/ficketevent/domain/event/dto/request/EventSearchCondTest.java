package com.example.ficketevent.domain.event.dto.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EventSearchCond DTO 테스트")
class EventSearchCondTest {

    @Test
    @DisplayName("모든 필드가 null인 기본 상태")
    void defaultValues() {
        EventSearchCond cond = new EventSearchCond();

        assertThat(cond.getEventId()).isNull();
        assertThat(cond.getEventTitle()).isNull();
        assertThat(cond.getCompanyId()).isNull();
        assertThat(cond.getAdminId()).isNull();
        assertThat(cond.getEventStageId()).isNull();
        assertThat(cond.getStartDate()).isNull();
        assertThat(cond.getEndDate()).isNull();
    }

    @Test
    @DisplayName("startDate와 endDate가 LocalDate 타입으로 설정된다")
    void dateFieldsAreLocalDate() {
        EventSearchCond cond = new EventSearchCond();
        LocalDate start = LocalDate.of(2024, 12, 1);
        LocalDate end = LocalDate.of(2024, 12, 31);

        cond.setStartDate(start);
        cond.setEndDate(end);

        assertThat(cond.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 1));
        assertThat(cond.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("모든 검색 조건을 설정하고 조회할 수 있다")
    void allFieldsSet() {
        EventSearchCond cond = new EventSearchCond();
        cond.setEventId(1L);
        cond.setEventTitle("뮤지컬 라이온킹");
        cond.setCompanyId(100L);
        cond.setAdminId(200L);
        cond.setEventStageId(300L);
        cond.setStartDate(LocalDate.of(2024, 12, 16));
        cond.setEndDate(LocalDate.of(2024, 12, 31));

        assertThat(cond.getEventId()).isEqualTo(1L);
        assertThat(cond.getEventTitle()).isEqualTo("뮤지컬 라이온킹");
        assertThat(cond.getCompanyId()).isEqualTo(100L);
        assertThat(cond.getAdminId()).isEqualTo(200L);
        assertThat(cond.getEventStageId()).isEqualTo(300L);
        assertThat(cond.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 16));
        assertThat(cond.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }
}
