package com.example.ficketevent.domain.event.controller;

import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchListRes;
import com.example.ficketevent.domain.event.dto.response.PagedResponse;
import com.example.ficketevent.domain.event.service.EventService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EventController - 관리자 행사 검색 API 테스트")
class EventSearchControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private EventController eventController;

    @Mock
    private EventService eventService;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(eventController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    @DisplayName("GET /api/v1/events/admin - 날짜 파라미터가 LocalDate로 바인딩된다")
    void searchWithDateParams() throws Exception {
        // given
        EventSearchListRes res = EventSearchListRes.builder()
                .eventId(1L)
                .eventTitle("뮤지컬 라이온킹")
                .stageName("블루스퀘어")
                .companyName("회사A")
                .adminName("관리자A")
                .startDate(LocalDateTime.of(2024, 12, 20, 19, 0))
                .endDate(LocalDateTime.of(2024, 12, 31, 19, 0))
                .build();

        PagedResponse<EventSearchListRes> pagedResponse = new PagedResponse<>(
                List.of(res), 0, 10, 1L, 1
        );

        given(eventService.searchEvent(any(), any())).willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/events/admin")
                        .param("startDate", "2024-12-01")
                        .param("endDate", "2024-12-31")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[0].eventId").value(1))
                .andExpect(jsonPath("$.content[0].eventTitle").value("뮤지컬 라이온킹"))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.totalPages").value(1));

        ArgumentCaptor<EventSearchCond> condCaptor = ArgumentCaptor.forClass(EventSearchCond.class);
        verify(eventService).searchEvent(condCaptor.capture(), any());

        EventSearchCond capturedCond = condCaptor.getValue();
        assertThat(capturedCond.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 1));
        assertThat(capturedCond.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("GET /api/v1/events/admin - 파라미터 없이 호출하면 모든 조건이 null이다")
    void searchWithNoParams() throws Exception {
        // given
        PagedResponse<EventSearchListRes> pagedResponse = new PagedResponse<>(
                List.of(), 0, 20, 0L, 0
        );
        given(eventService.searchEvent(any(), any())).willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/events/admin"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isEmpty())
                .andExpect(jsonPath("$.totalElements").value(0));

        ArgumentCaptor<EventSearchCond> condCaptor = ArgumentCaptor.forClass(EventSearchCond.class);
        verify(eventService).searchEvent(condCaptor.capture(), any());

        EventSearchCond capturedCond = condCaptor.getValue();
        assertThat(capturedCond.getEventId()).isNull();
        assertThat(capturedCond.getEventTitle()).isNull();
        assertThat(capturedCond.getCompanyId()).isNull();
        assertThat(capturedCond.getAdminId()).isNull();
        assertThat(capturedCond.getEventStageId()).isNull();
        assertThat(capturedCond.getStartDate()).isNull();
        assertThat(capturedCond.getEndDate()).isNull();
    }

    @Test
    @DisplayName("GET /api/v1/events/admin - 모든 검색 조건을 전달할 수 있다")
    void searchWithAllParams() throws Exception {
        // given
        PagedResponse<EventSearchListRes> pagedResponse = new PagedResponse<>(
                List.of(), 0, 10, 0L, 0
        );
        given(eventService.searchEvent(any(), any())).willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/events/admin")
                        .param("eventId", "5")
                        .param("eventTitle", "뮤지컬")
                        .param("companyId", "100")
                        .param("adminId", "200")
                        .param("eventStageId", "300")
                        .param("startDate", "2024-12-01")
                        .param("endDate", "2024-12-31")
                        .param("page", "1")
                        .param("size", "5"))
                .andExpect(status().isOk());

        ArgumentCaptor<EventSearchCond> condCaptor = ArgumentCaptor.forClass(EventSearchCond.class);
        verify(eventService).searchEvent(condCaptor.capture(), any());

        EventSearchCond capturedCond = condCaptor.getValue();
        assertThat(capturedCond.getEventId()).isEqualTo(5L);
        assertThat(capturedCond.getEventTitle()).isEqualTo("뮤지컬");
        assertThat(capturedCond.getCompanyId()).isEqualTo(100L);
        assertThat(capturedCond.getAdminId()).isEqualTo(200L);
        assertThat(capturedCond.getEventStageId()).isEqualTo(300L);
        assertThat(capturedCond.getStartDate()).isEqualTo(LocalDate.of(2024, 12, 1));
        assertThat(capturedCond.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
    }

    @Test
    @DisplayName("GET /api/v1/events/admin - 페이지네이션 파라미터가 올바르게 전달된다")
    void searchWithPagination() throws Exception {
        // given
        PagedResponse<EventSearchListRes> pagedResponse = new PagedResponse<>(
                List.of(), 2, 5, 30L, 6
        );
        given(eventService.searchEvent(any(), any())).willReturn(pagedResponse);

        // when & then
        mockMvc.perform(get("/api/v1/events/admin")
                        .param("page", "2")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(2))
                .andExpect(jsonPath("$.size").value(5))
                .andExpect(jsonPath("$.totalElements").value(30))
                .andExpect(jsonPath("$.totalPages").value(6));
    }
}
