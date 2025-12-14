package com.example.ficketqueue.global.redis;

import org.springframework.stereotype.Component;

/**
 * Redis Lua 스크립트 관리
 *
 * - 대기열: ZSET
 * - 순번 발급: INCR
 * - 입장 판단: Lua에서 원자적 처리
 */
@Component
public class RedisLuaScripts {

    /**
     * 대기열 진입
     * - 중복 방지
     * - 순번 발급 + ZSET 추가
     *
     * KEYS[1] = nextNumberKey
     * KEYS[2] = waitingZSetKey
     * ARGV[1] = userId
     *
     * return: 발급된 순번(score)
     */
    public String getEnterQueueScript() {
        return """
        local nextKey = KEYS[1]
        local waitingKey = KEYS[2]
        local userId = ARGV[1]

        -- 이미 대기열에 있으면 기존 score 반환
        local existingScore = redis.call('ZSCORE', waitingKey, userId)
        if existingScore then
            return tonumber(existingScore)
        end

        -- 순번 발급
        local seq = redis.call('INCR', nextKey)

        -- 대기열(ZSET)에 추가
        redis.call('ZADD', waitingKey, seq, userId)

        return seq
        """;
    }

    /**
     * 예매 화면 진입 시도
     * - 맨 앞(rank == 0)인지 확인
     * - 동시 접속 제한 확인
     * - 입장 시 대기열 제거 + 작업 슬롯 점유
     *
     * KEYS[1] = waitingZSetKey
     * KEYS[2] = currentNumberKey
     * KEYS[3] = workingUserKey
     *
     * ARGV[1] = userId
     * ARGV[2] = maxConcurrent
     * ARGV[3] = ttlSeconds
     *
     * return 1 = 입장 성공
     * return 0 = 입장 불가
     */
    public String getEnterTicketingScript() {
        return """
        -- 이미 입장한 사용자면 성공 (멱등성)
        if redis.call('EXISTS', KEYS[3]) == 1 then
            return 1
        end

        -- 대기열 순서 확인 (맨 앞만 허용)
        local rank = redis.call('ZRANK', KEYS[1], ARGV[1])
        if not rank or rank ~= 0 then
            return 0
        end

        -- 현재 입장 인원 확인
        local current = tonumber(redis.call('GET', KEYS[2]) or '0')
        local maxConcurrent = tonumber(ARGV[2])

        if current >= maxConcurrent then
            return 0
        end

        -- 입장 처리 (원자적 상태 전환)
        redis.call('ZREM', KEYS[1], ARGV[1])        -- 대기열 제거
        redis.call('INCR', KEYS[2])                -- 현재 입장 인원 증가
        redis.call('SET', KEYS[3], 1, 'EX', ARGV[3]) -- 작업 공간 TTL 설정

        return 1
        """;
    }

    /**
     * 예매 화면 나기기
     * - 작업 공간 해제
     * - currentNumber 감소
     *
     * KEYS[1] = currentNumberKey
     * KEYS[2] = workingUserKey
     *
     * return 1 = 퇴장 성공
     * return 0 = 이미 퇴장 상태
     */
    public String getLeaveTicketingScript() {
        return """
        if redis.call('DEL', KEYS[2]) == 1 then
            redis.call('DECR', KEYS[1])
            return 1
        end

        return 0
        """;
    }
}
