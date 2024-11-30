package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.global.config.redisson.DistributedLock;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class PreoccupyInternalService {

    private final RedissonClient redissonClient; // Redis와 상호작용하기 위한 Redisson 클라이언트

    /**
     * 좌석에 대해 분산 락을 획득하고 선점 처리
     *
     * @param lockName      락 키 (Redis에서 사용할 고유 락 이름)
     * @param userId        좌석을 선점하는 사용자 ID
     * @param seatMappingId 좌석 매핑 ID
     * @param eventScheduleId 이벤트 일정 ID
     */
    @DistributedLock(key = "#lockName") // AOP를 통해 분산 락을 적용
    public void lockSeat(String lockName, Long userId, Long seatMappingId, Long eventScheduleId) {
        // 좌석에 대한 선점 처리
        holdSeat(eventScheduleId, userId, seatMappingId);
    }

    /**
     * Redis에 사용자 및 좌석 정보를 저장하여 좌석 선점 처리
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @param userId          좌석을 선점하는 사용자 ID
     * @param seatMappingId   좌석 매핑 ID
     */
    public void holdSeat(Long eventScheduleId, Long userId, Long seatMappingId) {
        // 좌석 상태를 관리하는 해시 키: ficket:seats:<eventScheduleId>
        String seatKey = "ficket:seats:" + eventScheduleId;

        // 좌석 상태를 관리하는 RMap (Hash)
        RMap<String, String> seatStates = redissonClient.getMap(seatKey);

        String seatField = "seat_" + seatMappingId; // 필드: 좌석 ID

        // 좌석 정보를 저장
        seatStates.put(seatField, "{\"userId\":" + userId + "}");

        // 사용자 예약 정보를 관리하는 해시 키: ficket:user:<userId>:events
        String userKey = "ficket:user:" + userId + ":events";

        // 사용자 예약 정보를 관리하는 RMap (Hash)
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

        String eventField = "event_" + eventScheduleId; // 필드: 이벤트 ID
        userEvents.merge(eventField, "[" + seatMappingId + "]", (oldValue, newValue) -> {
            // 좌석 ID를 JSON 배열로 관리
            return mergeSeatIds(oldValue, seatMappingId);
        });
    }

    /**
     * 사용자 예약 정보에 좌석 ID를 병합
     *
     * @param existingSeats 기존 좌석 정보 (JSON 배열)
     * @param newSeatId     추가할 좌석 ID
     * @return 병합된 좌석 정보 (JSON 배열)
     */
    private String mergeSeatIds(String existingSeats, Long newSeatId) {
        if (existingSeats == null || existingSeats.isEmpty()) {
            return "[" + newSeatId + "]";
        }

        // 기존 좌석 ID 목록에 새 좌석 ID 추가
        existingSeats = existingSeats.substring(0, existingSeats.length() - 1); // 끝의 ']' 제거
        return existingSeats + "," + newSeatId + "]";
    }
}
