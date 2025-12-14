package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.global.utils.KeyHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 작업 공간 관리 서비스 클래스.
 */
@Service
@Transactional
@RequiredArgsConstructor
public class SlotService {

    private final RedisTemplate<String, Object> redis;


    /**
     * 최대 작업 가능한 슬롯 수 설정.
     *
     * @param eventId 이벤트 ID
     * @param maxSlot 최대 작업 가능한 슬롯 수
     */
    public void setMaxSlot(String eventId, int maxSlot) {
        String maxSlotKey = KeyHelper.maxConcurrentKey(eventId);
        redis.opsForValue().set(maxSlotKey, maxSlot);
    }

    /**
     * 현재 최대 작업 가능한 슬롯 수 조회
     *
     * @param eventId 이벤트 ID
     * @return 최대 슬롯 수, 설정이 없으면 null
     */
    public Integer getMaxSlot(String eventId) {
        String maxSlotKey = KeyHelper.maxConcurrentKey(eventId);
        Object value = redis.opsForValue().get(maxSlotKey);
        if (value == null) {
            return null;
        }
        return Integer.parseInt(value.toString());
    }

}