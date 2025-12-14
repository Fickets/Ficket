package com.example.ficketqueue.global.redis;

import org.springframework.stereotype.Component;

/**
 * Redis Lua 스크립트 관리
 */
@Component
public class RedisLuaScripts {

    /**
     * 대기열 진입 (순번 발급 + 중복 방지)
     */
    public String getEnterQueueScript() {
        return """
            local nextKey = KEYS[1]
            local userKey = KEYS[2]
            
            -- 이미 번호가 있으면 기존 번호 반환
            local existing = redis.call('GET', userKey)
            if existing then
                return tonumber(existing)
            end
            
            -- 새 번호 발급
            local nextNumber = redis.call('INCR', nextKey)
            redis.call('SET', userKey, nextNumber)
            
            return nextNumber
        """;
    }

    /**
     * 예매 화면 진입 허용 (currentNumber 증가 + TTL 설정)
     * KEYS[1] = currentNumberKey
     * KEYS[2] = workingUserKey
     * ARGV[1] = maxConcurrent
     * ARGV[2] = TTL(초)
     */
    public String getEnterTicketingScript() {
        return """
            local currentNumber = tonumber(redis.call('GET', KEYS[1]) or '0')
            local maxConcurrent = tonumber(ARGV[1])
            
            if currentNumber >= maxConcurrent then
                return 0  -- 입장 불가
            end
            
            -- 입장 허용
            redis.call('INCR', KEYS[1])
            redis.call('SET', KEYS[2], 1, 'EX', ARGV[2])
            
            return 1  -- 입장 성공
        """;
    }

    /**
     * currentNumber 감소 (사용자 퇴장 또는 TTL 만료 시)
     * KEYS[1] = currentNumberKey
     */
    public String getLeaveScreenScript() {
        return """
            local currentNumber = tonumber(redis.call('GET', KEYS[1]) or '0')
            if currentNumber <= 0 then
                return 0
            end
            
            redis.call('DECR', KEYS[1])
            return 1
        """;
    }
}
