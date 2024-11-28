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
     */
    @DistributedLock(key = "#lockName") // AOP를 통해 분산 락을 적용
    public void lockSeat(String lockName, Long userId, Long seatMappingId, Long eventScheduleId) {
        // 락을 획득한 상태에서 좌석 선점 처리
        holdSeat(lockName, userId, seatMappingId, eventScheduleId);
    }

    /**
     * Redis에 사용자 및 좌석 정보를 저장하여 좌석 선점 처리
     *
     * @param seatKey       좌석 키 (Redis에서 사용할 고유 키)
     * @param userId        좌석을 선점하는 사용자 ID
     * @param seatMappingId 좌석 매핑 ID
     */
    public void holdSeat(String seatKey, Long userId, Long seatMappingId, Long eventScheduleId) {
        // Redis의 Map 데이터 구조를 활용하여 사용자 및 좌석 정보를 저장
        RMap<String, String> lockMap = redissonClient.getMap(seatKey);
        lockMap.put("userId", userId.toString()); // 사용자 ID 저장
        lockMap.put("seatMappingId", seatMappingId.toString()); // 좌석 매핑 ID 저장
        lockMap.put("eventScheduleId", eventScheduleId.toString());
    }
}
