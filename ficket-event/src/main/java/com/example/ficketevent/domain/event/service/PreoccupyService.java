package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.UserServiceClient;
import com.example.ficketevent.domain.event.dto.common.UserSimpleDto;
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

    private final UserServiceClient userServiceClient;
    private final RedissonClient redissonClient;
    private final PreoccupyInternalService preoccupyInternalService;

    @Transactional
    public void preoccupySeat(SelectSeat request, Long userId) {
        UserSimpleDto user = userServiceClient.getUser(userId);

        log.info("요청한 유저의 ID: {}", user.getUserId());

        Set<Long> seatMappingIds = request.getSeatMappingIds();
        Long eventScheduleId = request.getEventScheduleId();

        // 해당 유저가 이미 예약한 좌석이 있는지 확인
        ensureUserHasNoSelectedSeats(eventScheduleId, user.getUserId());

        // 요청된 좌석 수와 예약 제한을 검증
        validateSeatCount(seatMappingIds, request.getReservationLimit());

        // 요청 좌석 중 이미 선점된 좌석이 있는지 확인
        validateSeatsAvailability(eventScheduleId, seatMappingIds);

        // 각 좌석을 사용자에 대해 잠금 처리
        seatMappingIds.forEach(seatMappingId -> lockSeat(eventScheduleId, user.getUserId(), seatMappingId));
    }

    private void ensureUserHasNoSelectedSeats(Long eventScheduleId, Long userId) {
        String userKey = "ficket:user:" + userId + ":events";
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

        // 특정 이벤트에서 사용자가 이미 예약한 좌석이 있는지 확인
        String eventField = "event_" + eventScheduleId;
        if (userEvents.containsKey(eventField)) {
            log.warn("사용자 {}가 이벤트 일정 {}에 대해 이미 예약된 좌석이 존재합니다.", userId, eventScheduleId);
            throw new BusinessException(ErrorCode.USER_ALREADY_HAS_RESERVED_SEATS);
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
            log.warn("요청된 좌석 수 {}가 예약 제한 {}을 초과했습니다.", requestedSeatCount, reservationLimit);
            throw new BusinessException(ErrorCode.EXCEED_SEAT_RESERVATION_LIMIT);
        }

    }

    private void validateSeatsAvailability(Long eventScheduleId, Set<Long> seatMappingIds) {
        String seatKey = "ficket:seats:" + eventScheduleId;
        RMap<String, String> seatStates = redissonClient.getMap(seatKey);

        for (Long seatMappingId : seatMappingIds) {
            String seatField = "seat_" + seatMappingId;
            if (seatStates.containsKey(seatField)) {
                log.warn("좌석 {}가 이미 선점되었습니다.", seatMappingId);
                throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
        }
    }


    private void lockSeat(Long eventScheduleId, Long userId, Long seatMappingId) {
        String lockKey = "seatLock:" + eventScheduleId + ":" + seatMappingId;
        preoccupyInternalService.lockSeat(lockKey, userId, seatMappingId, eventScheduleId);
        log.info("좌석 {}가 사용자 {}에 의해 선점되었습니다.", seatMappingId, userId);
    }

    public void releaseSeat(Long eventScheduleId, Set<Long> seatMappingIds, Long userId) {
        // 좌석 상태를 관리하는 해시 키: ficket:seats:<eventScheduleId>
        String seatKey = "ficket:seats:" + eventScheduleId;
        RMap<String, String> seatStates = redissonClient.getMap(seatKey);

        // 사용자 예약 정보를 관리하는 해시 키: ficket:user:<userId>:events
        String userKey = "ficket:user:" + userId + ":events";
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

        String eventField = "event_" + eventScheduleId; // 사용자 이벤트 필드

        // 1. 좌석 상태 및 잠금 키 삭제
        seatMappingIds.forEach(seatMappingId -> {
            String seatField = "seat_" + seatMappingId;
            String lockKey = "seatLock:" + eventScheduleId + ":" + seatMappingId;

            // 좌석 상태 삭제
            if (seatStates.containsKey(seatField)) {
                seatStates.remove(seatField);
                log.info("좌석 {}가 사용자 {}로부터 해제되었습니다.", seatMappingId, userId);
            } else {
                log.warn("좌석 {}는 이미 해제된 상태입니다.", seatMappingId);
            }

            // 좌석 잠금 키 삭제
            if (redissonClient.getKeys().delete(lockKey) > 0) {
                log.info("좌석 {}에 대한 잠금 키 {}가 삭제되었습니다.", seatMappingId, lockKey);
            } else {
                log.warn("잠금 키 {}는 존재하지 않아 삭제되지 않았습니다.", lockKey);
            }
        });

        // 2. 사용자 이벤트 정보 삭제
        if (userEvents.containsKey(eventField)) {
            userEvents.remove(eventField);
            log.info("사용자 {}의 이벤트 데이터가 삭제되었습니다. EventScheduleId: {}", userId, eventScheduleId);
        } else {
            log.warn("사용자 {}의 이벤트 데이터가 존재하지 않아 삭제할 수 없습니다. EventScheduleId: {}", userId, eventScheduleId);
        }
    }

}
