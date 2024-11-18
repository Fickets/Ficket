package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.response.SeatResponse;
import com.example.ficketevent.domain.event.dto.response.StageSeatResponse;
import com.example.ficketevent.domain.event.entity.EventStage;
import com.example.ficketevent.domain.event.mapper.StageSeatMapper;
import com.example.ficketevent.domain.event.repository.EventStageRepository;
import com.example.ficketevent.domain.event.repository.StageSeatRepository;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 행사장 좌석 정보를 관리하는 서비스 클래스.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageSeatService {

    private final EventStageRepository eventStageRepository;
    private final StageSeatRepository stageSeatRepository;
    private final StageSeatMapper stageSeatMapper;

    /**
     * 주어진 행사장(stageId)에 대한 좌석 목록을 조회하고, 결과를 캐싱합니다.
     *
     * @param stageId 조회할 행사장의 ID
     * @return 좌석 목록을 포함한 StageSeatResponse 객체
     */
    @Cacheable(cacheNames = "stage-seats", key = "#stageId", unless = "#result == null")
    public StageSeatResponse getSeats(Long stageId) {
        // 행사장 ID를 통해 EventStage 엔티티 조회
        EventStage eventStage = findEventStageById(stageId);

        // 조회된 행사장의 좌석 정보를 DTO로 변환
        List<SeatResponse> seatDtos = mapSeatsToDto(eventStage);

        // 변환된 좌석 DTO 목록을 StageSeatResponse로 래핑하여 반환
        return new StageSeatResponse(seatDtos);
    }

    /**
     * 행사장 ID를 사용하여 EventStage 엔티티를 조회합니다.
     * 해당 ID에 대한 데이터가 없을 경우 BusinessException을 발생시킵니다.
     *
     * @param stageId 조회할 행사장 ID
     * @return 조회된 EventStage 엔티티
     * @throws BusinessException 행사장을 찾을 수 없는 경우 발생
     */
    private EventStage findEventStageById(Long stageId) {
        return eventStageRepository.findById(stageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
    }

    /**
     * 주어진 행사장에 대한 좌석 정보를 조회하고, 이를 DTO로 변환합니다.
     *
     * @param eventStage 조회할 행사장(EventStage 엔티티)
     * @return 좌석 정보가 담긴 SeatResponse DTO 리스트
     */
    private List<SeatResponse> mapSeatsToDto(EventStage eventStage) {
        return stageSeatRepository.findByEventStage(eventStage)
                .stream()
                .map(stageSeatMapper::toStageSeatDto) // StageSeat를 SeatResponse로 변환
                .toList();
    }
}
