package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.AdminServiceClient;
import com.example.ficketevent.domain.event.dto.common.AdminDto;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import com.example.ficketevent.domain.event.dto.request.EventSearchCond;
import com.example.ficketevent.domain.event.dto.response.EventSearchListRes;
import com.example.ficketevent.domain.event.dto.response.EventSummaryProjection;
import com.example.ficketevent.domain.event.dto.response.PagedResponse;
import com.example.ficketevent.domain.event.repository.EventRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EventSearchServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private AdminServiceClient adminServiceClient;

    @Mock
    private CircuitBreakerRegistry circuitBreakerRegistry;

    @Mock
    private CircuitBreaker circuitBreaker;

    @BeforeEach
    void setUp() {
        lenient().when(circuitBreakerRegistry.circuitBreaker(anyString())).thenReturn(circuitBreaker);
        lenient().when(circuitBreaker.executeSupplier(any(Supplier.class)))
                .thenAnswer(invocation -> {
                    Supplier<?> supplier = invocation.getArgument(0);
                    return supplier.get();
                });
    }

    private EventSummaryProjection createProjection(Long eventId, String title, String stageName,
                                                     Long companyId, Long adminId,
                                                     LocalDateTime minDate, LocalDateTime maxDate) {
        EventSummaryProjection projection = mock(EventSummaryProjection.class);
        given(projection.getEventId()).willReturn(eventId);
        given(projection.getTitle()).willReturn(title);
        given(projection.getStageName()).willReturn(stageName);
        given(projection.getCompanyId()).willReturn(companyId);
        given(projection.getAdminId()).willReturn(adminId);
        given(projection.getMinEventDate()).willReturn(minDate);
        given(projection.getMaxEventDate()).willReturn(maxDate);
        return projection;
    }

    @Nested
    @DisplayName("searchEvent - 관리자 행사 검색")
    class SearchEvent {

        @Test
        @DisplayName("조건 없이 전체 검색 시 페이지네이션 결과를 반환한다")
        void searchAll() {
            // given
            EventSearchCond cond = new EventSearchCond();
            Pageable pageable = PageRequest.of(0, 10);

            EventSummaryProjection p1 = createProjection(
                    1L, "뮤지컬 라이온킹", "블루스퀘어", 100L, 200L,
                    LocalDateTime.of(2024, 12, 20, 19, 0),
                    LocalDateTime.of(2024, 12, 31, 19, 0)
            );
            EventSummaryProjection p2 = createProjection(
                    2L, "콘서트 IU", "올림픽홀", 101L, 201L,
                    LocalDateTime.of(2025, 1, 10, 18, 0),
                    LocalDateTime.of(2025, 1, 12, 18, 0)
            );

            given(eventRepository.searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(10), eq(0L)
            )).willReturn(List.of(p1, p2));

            given(eventRepository.countEventsByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(2L);

            given(adminServiceClient.getAdminsByIds(Set.of(200L, 201L)))
                    .willReturn(List.of(
                            new AdminDto(200L, "관리자A", "ADMIN"),
                            new AdminDto(201L, "관리자B", "ADMIN")
                    ));
            given(adminServiceClient.getCompaniesByIds(Set.of(100L, 101L)))
                    .willReturn(List.of(
                            new CompanyResponse(100L, "회사A"),
                            new CompanyResponse(101L, "회사B")
                    ));

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2L);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(10);

            EventSearchListRes first = result.getContent().get(0);
            assertThat(first.getEventId()).isEqualTo(1L);
            assertThat(first.getEventTitle()).isEqualTo("뮤지컬 라이온킹");
            assertThat(first.getStageName()).isEqualTo("블루스퀘어");
            assertThat(first.getCompanyName()).isEqualTo("회사A");
            assertThat(first.getAdminName()).isEqualTo("관리자A");
        }

        @Test
        @DisplayName("날짜 조건으로 검색 시 LocalDate 파라미터가 올바르게 전달된다")
        void searchByDateRange() {
            // given
            EventSearchCond cond = new EventSearchCond();
            cond.setStartDate(LocalDate.of(2024, 12, 1));
            cond.setEndDate(LocalDate.of(2024, 12, 31));
            Pageable pageable = PageRequest.of(0, 10);

            EventSummaryProjection p1 = createProjection(
                    1L, "뮤지컬 라이온킹", "블루스퀘어", 100L, 200L,
                    LocalDateTime.of(2024, 12, 20, 19, 0),
                    LocalDateTime.of(2024, 12, 25, 19, 0)
            );

            given(eventRepository.searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq(LocalDate.of(2024, 12, 1)),
                    eq(LocalDate.of(2024, 12, 31)),
                    eq(10), eq(0L)
            )).willReturn(List.of(p1));

            given(eventRepository.countEventsByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq(LocalDate.of(2024, 12, 1)),
                    eq(LocalDate.of(2024, 12, 31))
            )).willReturn(1L);

            given(adminServiceClient.getAdminsByIds(Set.of(200L)))
                    .willReturn(List.of(new AdminDto(200L, "관리자A", "ADMIN")));
            given(adminServiceClient.getCompaniesByIds(Set.of(100L)))
                    .willReturn(List.of(new CompanyResponse(100L, "회사A")));

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1L);

            verify(eventRepository).searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    eq(LocalDate.of(2024, 12, 1)),
                    eq(LocalDate.of(2024, 12, 31)),
                    eq(10), eq(0L)
            );
        }

        @Test
        @DisplayName("제목과 이벤트 ID로 복합 조건 검색이 동작한다")
        void searchByMultipleConditions() {
            // given
            EventSearchCond cond = new EventSearchCond();
            cond.setEventId(5L);
            cond.setEventTitle("뮤지컬");
            cond.setCompanyId(100L);
            Pageable pageable = PageRequest.of(0, 10);

            EventSummaryProjection p1 = createProjection(
                    5L, "뮤지컬 위키드", "세종문화회관", 100L, 300L,
                    LocalDateTime.of(2025, 1, 1, 19, 0),
                    LocalDateTime.of(2025, 2, 28, 19, 0)
            );

            given(eventRepository.searchEventByCond(
                    eq(5L), eq("뮤지컬"), eq(100L), isNull(), isNull(),
                    isNull(), isNull(), eq(10), eq(0L)
            )).willReturn(List.of(p1));

            given(eventRepository.countEventsByCond(
                    eq(5L), eq("뮤지컬"), eq(100L), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(1L);

            given(adminServiceClient.getAdminsByIds(Set.of(300L)))
                    .willReturn(List.of(new AdminDto(300L, "관리자C", "ADMIN")));
            given(adminServiceClient.getCompaniesByIds(Set.of(100L)))
                    .willReturn(List.of(new CompanyResponse(100L, "회사A")));

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(1);
            EventSearchListRes res = result.getContent().get(0);
            assertThat(res.getEventId()).isEqualTo(5L);
            assertThat(res.getEventTitle()).isEqualTo("뮤지컬 위키드");
            assertThat(res.getCompanyName()).isEqualTo("회사A");
            assertThat(res.getAdminName()).isEqualTo("관리자C");
        }

        @Test
        @DisplayName("검색 결과가 없으면 빈 페이지를 반환한다")
        void searchEmpty() {
            // given
            EventSearchCond cond = new EventSearchCond();
            cond.setEventTitle("존재하지않는공연");
            Pageable pageable = PageRequest.of(0, 10);

            given(eventRepository.searchEventByCond(
                    isNull(), eq("존재하지않는공연"), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(10), eq(0L)
            )).willReturn(List.of());

            given(eventRepository.countEventsByCond(
                    isNull(), eq("존재하지않는공연"), isNull(), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(0L);

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
            assertThat(result.getTotalPages()).isZero();

            verifyNoInteractions(adminServiceClient);
        }

        @Test
        @DisplayName("페이지네이션 offset이 올바르게 계산된다")
        void paginationOffset() {
            // given
            EventSearchCond cond = new EventSearchCond();
            Pageable pageable = PageRequest.of(2, 5); // 3번째 페이지, 페이지당 5개 -> offset=10

            given(eventRepository.searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(5), eq(10L)
            )).willReturn(List.of());

            given(eventRepository.countEventsByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(15L);

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getPage()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(15L);
            assertThat(result.getTotalPages()).isEqualTo(3);

            verify(eventRepository).searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(5), eq(10L)
            );
        }

        @Test
        @DisplayName("totalPages 계산 - 나머지가 있으면 올림한다")
        void totalPagesRoundsUp() {
            // given
            EventSearchCond cond = new EventSearchCond();
            Pageable pageable = PageRequest.of(0, 10);

            given(eventRepository.searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(10), eq(0L)
            )).willReturn(List.of());

            given(eventRepository.countEventsByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(25L);

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getTotalPages()).isEqualTo(3); // ceil(25/10) = 3
        }

        @Test
        @DisplayName("동일 adminId/companyId를 가진 여러 projection은 배치 호출이 중복 없이 수행된다")
        void batchCallsDeduplicateIds() {
            // given
            EventSearchCond cond = new EventSearchCond();
            Pageable pageable = PageRequest.of(0, 10);

            EventSummaryProjection p1 = createProjection(
                    1L, "공연A", "홀A", 100L, 200L,
                    LocalDateTime.of(2024, 12, 20, 19, 0),
                    LocalDateTime.of(2024, 12, 25, 19, 0)
            );
            EventSummaryProjection p2 = createProjection(
                    2L, "공연B", "홀B", 100L, 200L,
                    LocalDateTime.of(2025, 1, 1, 19, 0),
                    LocalDateTime.of(2025, 1, 5, 19, 0)
            );

            given(eventRepository.searchEventByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull(), eq(10), eq(0L)
            )).willReturn(List.of(p1, p2));

            given(eventRepository.countEventsByCond(
                    isNull(), isNull(), isNull(), isNull(), isNull(),
                    isNull(), isNull()
            )).willReturn(2L);

            given(adminServiceClient.getAdminsByIds(Set.of(200L)))
                    .willReturn(List.of(new AdminDto(200L, "관리자A", "ADMIN")));
            given(adminServiceClient.getCompaniesByIds(Set.of(100L)))
                    .willReturn(List.of(new CompanyResponse(100L, "회사A")));

            // when
            PagedResponse<EventSearchListRes> result = eventService.searchEvent(cond, pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getContent().get(0).getCompanyName()).isEqualTo("회사A");
            assertThat(result.getContent().get(1).getCompanyName()).isEqualTo("회사A");
            assertThat(result.getContent().get(0).getAdminName()).isEqualTo("관리자A");
            assertThat(result.getContent().get(1).getAdminName()).isEqualTo("관리자A");

            // Set에 의해 중복 제거되므로 1번만 호출
            verify(adminServiceClient).getAdminsByIds(Set.of(200L));
            verify(adminServiceClient).getCompaniesByIds(Set.of(100L));
        }
    }
}
