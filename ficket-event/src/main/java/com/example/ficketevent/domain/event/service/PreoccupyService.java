package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.UserServiceClient;
import com.example.ficketevent.domain.event.dto.common.UserSimpleDto;
import com.example.ficketevent.domain.event.dto.request.SelectSeat;
import com.example.ficketevent.domain.event.dto.request.SelectSeatInfo;
import com.example.ficketevent.domain.event.dto.response.ReservedSeatInfo;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.CircuitBreakerUtils;
import com.example.ficketevent.global.utils.RateLimiterUtils;
import com.example.ficketevent.global.utils.RedisKeyHelper;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBucket;
import org.redisson.api.RKeys;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.example.ficketevent.global.utils.RateLimiterUtils.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreoccupyService {

    private final UserServiceClient userServiceClient;
    private final RedissonClient redissonClient;
    private final PreoccupyInternalService preoccupyInternalService;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final RateLimiterRegistry rateLimiterRegistry;

    @Transactional
    public void lockSeat(SelectSeat request, Long userId) {
        String limiterName = "preoccupySeatLimiter";

        executeWithRateLimiter(
                rateLimiterRegistry,
                limiterName,
                () -> {
                    preoccupySeat(request, userId);
                    return null; // Supplier<T>이므로 Void를 처리하기 위해 null 반환
                }
        );
    }


    public void preoccupySeat(SelectSeat request, Long userId) {
        UserSimpleDto user = CircuitBreakerUtils.executeWithCircuitBreaker(circuitBreakerRegistry,
                "getUserCircuitBreaker",
                () -> userServiceClient.getUser(userId)
        );

        log.info("요청한 유저의 ID: {}", user.getUserId());

        List<SelectSeatInfo> selectSeatInfoList = request.getSelectSeatInfoList();
        Set<Long> seatMappingIds = selectSeatInfoList.stream().map(SelectSeatInfo::getSeatMappingId).collect(Collectors.toSet());

        Long eventScheduleId = request.getEventScheduleId();

        // 해당 유저가 이미 예약한 좌석이 있는지 확인
        ensureUserHasNoSelectedSeats(eventScheduleId, user.getUserId());

        // 요청된 좌석 수와 예약 제한을 검증
        validateSeatCount(seatMappingIds, request.getReservationLimit());

        // 요청 좌석 중 이미 선점된 좌석이 있는지 확인
        validateSeatsAvailability(eventScheduleId, seatMappingIds);

        // 각 좌석을 사용자에 대해 잠금 처리
        selectSeatInfoList.forEach(selectSeatInfo -> lockSeat(eventScheduleId, user.getUserId(), selectSeatInfo.getSeatMappingId(), selectSeatInfo.getSeatGrade(), selectSeatInfo.getSeatPrice()));
    }

    private void ensureUserHasNoSelectedSeats(Long eventScheduleId, Long userId) {
        String userKey = RedisKeyHelper.getUserKey(userId); // 키 생성
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

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
        String seatKey = RedisKeyHelper.getSeatKey(eventScheduleId); // 키 생성
        RMap<String, String> seatStates = redissonClient.getMap(seatKey);

        for (Long seatMappingId : seatMappingIds) {
            String seatField = "seat_" + seatMappingId;
            if (seatStates.containsKey(seatField)) {
                log.warn("좌석 {}가 이미 선점되었습니다.", seatMappingId);
                throw new BusinessException(ErrorCode.SEAT_ALREADY_RESERVED);
            }
        }
    }

    private void lockSeat(Long eventScheduleId, Long userId, Long seatMappingId, String seatGrade, BigDecimal seatPrice) {
        String lockKey = RedisKeyHelper.getLockKey(eventScheduleId, seatMappingId); // 키 생성
        preoccupyInternalService.lockSeat(lockKey, userId, seatMappingId, eventScheduleId, seatGrade, seatPrice);
        log.info("좌석 {}가 사용자 {}에 의해 선점되었습니다. (등급: {}, 가격: {})", seatMappingId, userId, seatGrade, seatPrice);
    }

    public void releaseSeat(Long eventScheduleId, Set<Long> seatMappingIds, Long userId) {
        String seatKey = RedisKeyHelper.getSeatKey(eventScheduleId); // 키 생성
        RMap<String, String> seatStates = redissonClient.getMap(seatKey);

        String userKey = RedisKeyHelper.getUserKey(userId); // 키 생성
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

        String eventField = "event_" + eventScheduleId;

        seatMappingIds.forEach(seatMappingId -> {
            String seatField = "seat_" + seatMappingId;
            String lockKey = RedisKeyHelper.getLockKey(eventScheduleId, seatMappingId); // 키 생성

            if (seatStates.containsKey(seatField)) {
                seatStates.remove(seatField);
                log.info("좌석 {}가 사용자 {}로부터 해제되었습니다.", seatMappingId, userId);
            }

            if (redissonClient.getKeys().delete(lockKey) > 0) {
                log.info("좌석 {}에 대한 잠금 키 {}가 삭제되었습니다.", seatMappingId, lockKey);
            }
        });

        if (userEvents.containsKey(eventField)) {
            userEvents.remove(eventField);
            log.info("사용자 {}의 이벤트 데이터가 삭제되었습니다. EventScheduleId: {}", userId, eventScheduleId);
        }
    }

}
