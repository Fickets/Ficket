package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.response.*;
import com.example.ficketevent.domain.event.entity.EventStage;
import com.example.ficketevent.domain.event.mapper.StageSeatMapper;
import com.example.ficketevent.domain.event.repository.EventStageRepository;
import com.example.ficketevent.domain.event.repository.SeatMappingRepository;
import com.example.ficketevent.domain.event.repository.StageSeatRepository;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

/**
 * 행사장 좌석 정보를 관리하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageSeatService {

    private final EventStageRepository eventStageRepository;
    private final StageSeatRepository stageSeatRepository;
    private final StageSeatMapper stageSeatMapper;
    private final SeatMappingRepository seatMappingRepository;
    private final RedissonClient redissonClient;

    private static final String REDIS_SEAT_KEY_PREFIX = "Ficket_";

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

    /**
     * 좌석 조회
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 좌석 상태 목록
     */
    public List<SeatStatusResponse> getSeatStatusesByEventSchedule(Long eventScheduleId) {
        try {
            return getAvailableSeatsAsync(eventScheduleId);
        } catch (Exception e) {
            log.error("좌석 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("좌석 조회 중 문제가 발생했습니다.");
        }
    }

    /**
     * 비동기 좌석 조회
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 좌석 상태 목록
     */
    private List<SeatStatusResponse> getAvailableSeatsAsync(Long eventScheduleId) throws ExecutionException, InterruptedException {
        // Redis 좌석 상태 비동기 조회
        CompletableFuture<Set<Long>> redisFuture = CompletableFuture.supplyAsync(() -> getLockedSeatFromRedis(eventScheduleId));

        // 데이터베이스 좌석 정보 비동기 조회
        CompletableFuture<List<SeatInfo>> dbFuture = CompletableFuture.supplyAsync(() -> seatMappingRepository.findSeatInfoByEventScheduleId(eventScheduleId));

        // 병렬 처리 결과 병합
        CompletableFuture.allOf(redisFuture, dbFuture).join();

        Set<Long> lockedSeats = redisFuture.get();
        List<SeatInfo> allSeats = dbFuture.get();

        // 좌석 상태 병합
        return allSeats.stream()
                .map(seat -> SeatStatusResponse.builder()
                        .seatMappingId(seat.getSeatMappingId())
                        .seatX(seat.getSeatX())
                        .seatY(seat.getSeatY())
                        .seatGrade(seat.getSeatGrade())
                        .seatRow(seat.getSeatRow())
                        .seatCol(seat.getSeatCol())
                        .status(determineSeatStatus(seat.getSeatMappingId(), lockedSeats, seat.getPurchased()))
                        .build())
                .toList();
    }

    private Set<Long> getLockedSeatFromRedis(Long eventScheduleId) {
        Set<Long> lockedSeatSet = new HashSet<>();

        // Redis에서 이벤트 스케줄과 관련된 모든 좌석 키를 검색
        String pattern = REDIS_SEAT_KEY_PREFIX + eventScheduleId + "_*";
        Iterable<String> keys = redissonClient.getKeys().getKeysByPattern(pattern);

        for (String key : keys) {
            // 좌석 ID 추출 (예: Ficket_1_5 -> 5)
            String seatIdStr = key.substring((REDIS_SEAT_KEY_PREFIX + eventScheduleId + "_").length());
            Long seatId = Long.valueOf(seatIdStr);

            lockedSeatSet.add(seatId);
        }

        return lockedSeatSet;
    }

    /**
     * 좌석 상태 결정
     *
     * @param seatMappingId 좌석 매핑 ID
     * @param lockedSeats   Redis에서 가져온 선점된 좌석 목록
     * @param purchased     구매 여부
     * @return 좌석 상태
     */
    private String determineSeatStatus(Long seatMappingId, Set<Long> lockedSeats, Boolean purchased) {
        if (Boolean.TRUE.equals(purchased)) {
            return "PURCHASED";
        } else if (lockedSeats.contains(seatMappingId)) {
            return "LOCKED";
        }
        return "AVAILABLE";
    }

    /**
     * 특정 이벤트 일정에 대한 등급별 남은 좌석 수를 조회합니다.
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 등급별 남은 좌석 수 리스트
     */
    public List<SeatCntByGrade> getRemainingSeatsByGrade(Long eventScheduleId) {
        return seatMappingRepository.findPartitionSeatCounts(eventScheduleId);
    }

}
