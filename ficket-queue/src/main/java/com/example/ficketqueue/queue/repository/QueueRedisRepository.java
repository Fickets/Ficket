package com.example.ficketqueue.queue.repository;

import com.example.ficketqueue.global.redis.RedisLuaScripts;
import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Redis 기반 대기열 Repository 구현체
 */
@Repository
@RequiredArgsConstructor
public class QueueRedisRepository implements QueueRepository {

    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLuaScripts redisLuaScripts;

    private static final int TICKETING_TTL_SECONDS = 20 * 60; // 20분

    /**
     * 대기열 진입 - 순번 발급 + ZSET에 추가
     */
    @Override
    public Long enterQueue(String userId, String eventId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getEnterQueueScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(
                script,
                List.of(
                        KeyHelper.nextNumberKey(eventId),
                        KeyHelper.waitingZSetKey(eventId)
                ),
                userId
        );
    }

    @Override
    public Long leaveQueue(String userId, String eventId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getLeaveQueueScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(
                script,
                List.of(
                        KeyHelper.waitingZSetKey(eventId)
                ),
                userId
        );
    }

    /**
     * 예매 화면 진입 허용 - currentNumber 증가 + TTL 설정
     * @return 1 = 입장 성공, 0 = 입장 불가
     */
    @Override
    public Long enterTicketing(String userId, String eventId) {
        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getEnterTicketingScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(
                script,
                List.of(
                        KeyHelper.waitingZSetKey(eventId),
                        KeyHelper.currentNumberKey(eventId),
                        KeyHelper.workingUserKey(eventId, userId)
                ),
                userId,
                redisTemplate.opsForValue()
                        .get(KeyHelper.maxConcurrentKey(eventId)),
                TICKETING_TTL_SECONDS
        );
    }


    /**
     * 예매 화면 나가기
     */
    @Override
    public Long leaveTicketing(String userId, String eventId) {
        String currentNumberKey = KeyHelper.currentNumberKey(eventId);
        String workingUserKey = KeyHelper.workingUserKey(eventId, userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getLeaveTicketingScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(
                script,
                List.of(currentNumberKey, workingUserKey)
        );
    }

    /**
     * 대기열 상태 조회 - 내 앞에 남은 인원 수, 예매 화면 입장 여부
     */
    @Override
    public MyQueueStatusResponse getQueueStatus(String userId, String eventId) {
        String waitingZSetKey = KeyHelper.waitingZSetKey(eventId);
        String workingUserKey = KeyHelper.workingUserKey(eventId, userId);

        // ZRANK로 내 앞에 남은 인원 수 조회
        Long rank = redisTemplate.opsForZSet().rank(waitingZSetKey, userId);
        Long waitingAhead = rank != null ? rank : -1L;

        // 대기열에 남아 있는 총 인원 수 조회
        Long totalWaitingNumber = redisTemplate.opsForZSet().size(waitingZSetKey);

        // 현재 예매 화면 접속 여부 확인
        Boolean canEnterTicketing = redisTemplate.hasKey(workingUserKey);

        return MyQueueStatusResponse.of(waitingAhead, totalWaitingNumber, canEnterTicketing);
    }

}
