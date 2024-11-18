package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.response.EventStageResponse;
import com.example.ficketevent.domain.event.dto.response.EventStageListResponse;
import com.example.ficketevent.domain.event.mapper.EventStageMapper;
import com.example.ficketevent.domain.event.repository.EventStageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 행사장(EventStage) 관련 데이터를 관리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventStageService {

    private final EventStageRepository eventStageRepository;
    private final EventStageMapper eventStageMapper;

    /**
     * 모든 행사장 정보를 조회하고, 결과를 캐싱합니다.
     *
     * @return 행사장 목록을 포함한 EventStageListResponse 객체
     */
    @Cacheable(
            cacheNames = "stages", // 캐시 이름 설정
            key = "'all'",         // 고정된 키 값 사용
            cacheManager = "cacheManager", // 사용할 캐시 매니저 지정
            unless = "#result == null"     // 결과가 null이면 캐싱하지 않음
    )
    public EventStageListResponse getEventStages() {
        List<EventStageResponse> eventStageResponses = mapEventStagesToDto();
        return new EventStageListResponse(eventStageResponses);
    }

    /**
     * 모든 행사장 데이터를 조회하여 DTO로 변환합니다.
     *
     * @return 행사장 정보 리스트(EventStageResponse DTO 리스트)
     */
    private List<EventStageResponse> mapEventStagesToDto() {
        return eventStageRepository.findAll()
                .stream()
                .map(eventStageMapper::toEventStageDto) // Entity를 DTO로 변환
                .toList();
    }
}
