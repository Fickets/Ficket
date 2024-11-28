package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.request.SelectSeat;
import com.example.ficketevent.domain.event.dto.response.ReservedSeatInfo;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreoccupyService {

    private static final String REDIS_SEAT_KEY_PREFIX = "Ficket_";

    private final RedissonClient redissonClient;
    private final PreoccupyInternalService preoccupyInternalService;

    /**
     * 사용자 좌석 선점 처리
     *
     * @param request 좌석 선점 요청 (좌석 매핑 ID와 예약 제한 포함)
     * @param userId  좌석을 선점하려는 사용자 ID
     */
    @Transactional
    public void preoccupySeat(SelectSeat request, Long userId) {
        Set<Long> seatMappingIds = request.getSeatMappingIds();
        Long eventScheduleId = request.getEventScheduleId();

        // 사용자가 이미 좌석을 선택했는지 확인
        ensureUserHasNoSelectedSeats(eventScheduleId, userId);

        // 요청된 좌석 수와 예약 제한을 검증
        validateSeatCount(seatMappingIds, request.getReservationLimit());

        // 요청 좌석 중 이미 선점된 좌석이 있는지 확인
        validateSeatsAvailability(eventScheduleId, seatMappingIds);

        // 각 좌석을 사용자에 대해 잠금 처리
        seatMappingIds.forEach(seatMappingId -> lockSeat(eventScheduleId, userId, seatMappingId));
    }

    /**
     * 사용자가 이미 좌석을 선택했는지 확인
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @param userId          사용자 ID
     */
    private void ensureUserHasNoSelectedSeats(Long eventScheduleId, Long userId) {
        RKeys keys = redissonClient.getKeys();
        // Redis에서 해당 이벤트 일정과 관련된 모든 좌석 잠금 키 검색
        Iterable<String> redisKeys = keys.getKeysByPattern(REDIS_SEAT_KEY_PREFIX + eventScheduleId + "_" + "*");

        // 검색된 각 키에 대해 좌석 정보를 확인
        for (String redisKey : redisKeys) {
            RBucket<ReservedSeatInfo> seatBucket = redissonClient.getBucket(redisKey);
            ReservedSeatInfo seatInfo = seatBucket.get();
            if (seatInfo != null && seatInfo.getUserId().equals(userId)) {
                log.warn("사용자 {}가 이벤트 일정 {}에 대해 이미 예약된 좌석이 존재합니다.", userId, eventScheduleId);
                throw new BusinessException(ErrorCode.USER_ALREADY_HAS_RESERVED_SEATS);
            }
        }
    }

    /**
     * 요청된 좌석 수와 예약 제한을 검증
     *
     * @param seatMappingIds   요청된 좌석 매핑 ID 집합
     * @param reservationLimit 최대 예약 가능한 좌석 수
     */
    private void validateSeatCount(Set<Long> seatMappingIds, Integer reservationLimit) {
        int requestedSeatCount = seatMappingIds.size();
        if (requestedSeatCount == 0) {
            log.error("선택된 좌석이 없습니다.");
            throw new BusinessException(ErrorCode.EMPTY_SEATS_EXCEPTION);
        }

        if (requestedSeatCount > reservationLimit) {
            log.error("요청된 좌석 수 {}가 예약 제한 {}을 초과했습니다.", requestedSeatCount, reservationLimit);
            throw new BusinessException(ErrorCode.EXCEED_SEAT_RESERVATION_LIMIT);
        }
    }

    /**
     * 요청된 좌석 중 이미 선점된 좌석이 있는지 확인
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @param seatMappingIds  요청된 좌석 매핑 ID 집합
     */
    private void validateSeatsAvailability(Long eventScheduleId, Set<Long> seatMappingIds) {
        for (Long seatMappingId : seatMappingIds) {
            String redisSeatKey = REDIS_SEAT_KEY_PREFIX + eventScheduleId + seatMappingId;
            RBucket<ReservedSeatInfo> seatBucket = redissonClient.getBucket(redisSeatKey);

            if (seatBucket.isExists()) {
                ReservedSeatInfo seatInfo = seatBucket.get();
                log.warn("좌석 {}은 이미 사용자 {}에 의해 예약되었습니다.", seatMappingId, seatInfo.getUserId());
                throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
        }
    }

    /**
     * 특정 좌석을 사용자에 대해 잠금 처리
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @param userId          사용자 ID
     * @param seatMappingId   좌석 매핑 ID
     */
    private void lockSeat(Long eventScheduleId, Long userId, Long seatMappingId) {
        String redisSeatKey = REDIS_SEAT_KEY_PREFIX + eventScheduleId + "_" + seatMappingId; // Redis에서 사용할 좌석 키 생성
        preoccupyInternalService.lockSeat(redisSeatKey, userId, seatMappingId, eventScheduleId); // 내부 서비스 호출로 좌석 잠금 처리
    }

    /**
     * 특정 좌석을 사용자로부터 해제 처리
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @param seatMappingIds  해제할 좌석 매핑 ID 목록
     * @param userId          좌석을 해제하려는 사용자 ID
     */
    public void releaseSeat(Long eventScheduleId, Set<Long> seatMappingIds, Long userId) {
        for (Long seatMappingId : seatMappingIds) {
            String redisSeatKey = REDIS_SEAT_KEY_PREFIX + eventScheduleId + "_" + seatMappingId;

            // Redis Hash에서 좌석 정보 가져오기
            RMap<String, String> seatMap = redissonClient.getMap(redisSeatKey);

            // 예약된 사용자 정보 확인
            String reservedUserId = seatMap.get("userId");
            if (reservedUserId == null) {
                throw new BusinessException(ErrorCode.SEAT_NOT_RESERVED); // 좌석이 예약되지 않은 경우
            }

            // 예약자가 현재 사용자와 다른 경우 예외 발생
            if (!reservedUserId.equals(String.valueOf(userId))) {
                throw new BusinessException(ErrorCode.SEAT_RESERVED_BY_ANOTHER_USER); // 다른 사용자가 예약한 좌석
            }

            // 좌석 예약 정보 삭제
            seatMap.delete();
            log.info("좌석 {}가 사용자 {}로부터 해제되었습니다.", seatMappingId, userId);
        }
    }
}
