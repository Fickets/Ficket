package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.global.config.redisson.DistributedLock;
import com.example.ficketevent.global.config.redisson.RedisTTLConstants;
import com.example.ficketevent.global.utils.RedisKeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PreoccupyInternalService {

    private final RedissonClient redissonClient;

    /**
     * 좌석에 대한 분산 락을 획득하고 선점 처리를 수행합니다.
     *
     * @param lockName        Redis에서 사용할 고유 락 키.
     * @param userId          좌석을 선점하는 사용자 ID.
     * @param seatMappingId   좌석 매핑 ID.
     * @param eventScheduleId 이벤트 일정 ID.
     * @param seatGrade       좌석 등급.
     * @param seatPrice       좌석 가격.
     */
    @DistributedLock(key = "#lockName")
    public void lockSeat(String lockName, Long userId, Long seatMappingId, Long eventScheduleId, String seatGrade, BigDecimal seatPrice) {
        log.info("락 획득 시도: {}", lockName);
        preoccupySeat(eventScheduleId, userId, seatMappingId, seatGrade, seatPrice);
    }

    /**
     * Redis에 사용자 및 좌석 정보를 저장하여 좌석 선점 처리를 수행합니다.
     *
     * @param eventScheduleId 이벤트 일정 ID.
     * @param userId          좌석을 선점하는 사용자 ID.
     * @param seatMappingId   좌석 매핑 ID.
     * @param seatGrade       좌석 등급.
     * @param seatPrice       좌석 가격.
     */
    public void preoccupySeat(Long eventScheduleId, Long userId, Long seatMappingId, String seatGrade, BigDecimal seatPrice) {
        storeSeatInfo(eventScheduleId, userId, seatMappingId, seatGrade, seatPrice);
        storeUserEventInfo(eventScheduleId, userId, seatMappingId);
    }

    private void storeSeatInfo(Long eventScheduleId, Long userId, Long seatMappingId, String seatGrade, BigDecimal seatPrice) {
        String seatKey = RedisKeyHelper.getSeatKey(eventScheduleId);  // "seat_{eventScheduleId}" 형태의 키 생성
        RMapCache<String, String> seatStates = redissonClient.getMapCache(seatKey); // TTL 지원하는 MapCache 사용

        String seatField = generateSeatField(seatMappingId);  // "seat_101" 형태의 필드명 생성
        String seatInfo = generateSeatInfo(userId, seatGrade, seatPrice); // 좌석 정보 JSON 생성

        // 좌석 정보 저장 + 개별 TTL 설정 (9분 후 자동 만료)
        seatStates.put(seatField, seatInfo, 540L, RedisTTLConstants.SEAT_LOCK_TIME_UNIT);

        log.info("이벤트 {}의 좌석 {} 정보가 {}초 TTL과 함께 저장되었습니다.", eventScheduleId, seatMappingId, RedisTTLConstants.SEAT_LOCK_LEASE_TIME);
    }

    private void storeUserEventInfo(Long eventScheduleId, Long userId, Long seatMappingId) {
        String userKey = RedisKeyHelper.getUserKey(userId);
        RMapCache<String, String> userEvents = redissonClient.getMapCache(userKey);

        String eventField = generateEventField(eventScheduleId);

        userEvents.put(eventField, userEvents.compute(eventField,
                        (key, existingSeats) -> mergeSeatIds(existingSeats, seatMappingId)),
                540L, RedisTTLConstants.SEAT_LOCK_TIME_UNIT);

        log.info("사용자 {}의 이벤트 {} 좌석 정보를 {}초 TTL과 함께 저장했습니다.", userId, eventScheduleId, RedisTTLConstants.SEAT_LOCK_LEASE_TIME);
    }


    private String generateSeatField(Long seatMappingId) {
        return "seat_" + seatMappingId;
    }

    private String generateEventField(Long eventScheduleId) {
        return "event_" + eventScheduleId;
    }

    private String formatSeatId(Long seatMappingId) {
        return "[" + seatMappingId + "]";
    }

    private String generateSeatInfo(Long userId, String seatGrade, BigDecimal seatPrice) {
        return String.format("{\"userId\":%d, \"seatGrade\":\"%s\", \"seatPrice\":%s}", userId, seatGrade, seatPrice);
    }

    /**
     * 기존 좌석 ID 목록에 새로운 좌석 ID를 병합합니다.
     *
     * @param existingSeats 기존 좌석 ID 목록(JSON 배열).
     * @param newSeatId     새로 추가할 좌석 ID.
     * @return 병합된 좌석 ID 목록(JSON 배열).
     */
    private String mergeSeatIds(String existingSeats, Long newSeatId) {
        if (existingSeats == null || existingSeats.isBlank()) {
            return formatSeatId(newSeatId);
        }

        String trimmedSeats = existingSeats.substring(0, existingSeats.length() - 1);
        return trimmedSeats + "," + newSeatId + "]";
    }
}
