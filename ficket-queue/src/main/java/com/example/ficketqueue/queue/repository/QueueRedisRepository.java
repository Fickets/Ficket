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

    /**
     * 대기열 진입 - 순번 발급 + 중복 방지
     */
    @Override
    public Long enterQueue(String eventId, String userId) {
        String nextNumberKey = KeyHelper.nextNumberKey(eventId);
        String userNumberKey = KeyHelper.userQueueNumberKey(eventId, userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getEnterQueueScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(script, List.of(nextNumberKey, userNumberKey));
    }

    /**
     * 대기열 상태 조회 - 내 번호,
     */
    @Override
    public MyQueueStatusResponse getQueueStatus(String eventId, String userId) {
        String userNumberKey = KeyHelper.userQueueNumberKey(eventId, userId);
        String currentNumberKey = KeyHelper.currentNumberKey(eventId);
        String workingUserKey = KeyHelper.workingUserKey(eventId, userId);

        Long myNumber = (Long) redisTemplate.opsForValue().get(userNumberKey);
        Long currentNumber = (Long) redisTemplate.opsForValue().get(currentNumberKey);
        Boolean canEnterScreen = redisTemplate.hasKey(workingUserKey);

        return MyQueueStatusResponse.of(myNumber, currentNumber, canEnterScreen);
    }

    /**
     * 예매 화면 진입 허용 - currentNumber 증가 + TTL 설정
     * @return 1 = 입장 성공, 0 = 입장 불가
     */
    @Override
    public Long enterTicketing(String eventId, String userId, int maxConcurrent, int ttlSeconds) {
        String currentNumberKey = KeyHelper.currentNumberKey(eventId);
        String workingUserKey = KeyHelper.workingUserKey(eventId, userId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getEnterTicketingScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(
                script,
                List.of(currentNumberKey, workingUserKey),
                maxConcurrent,
                ttlSeconds
        );
    }

    /**
     * 예매 화면 퇴장 시 currentNumber 감소
     * @return 1 = 감소 성공, 0 = 이미 0
     */
    @Override
    public Long leaveTicketing(String eventId) {
        String currentNumberKey = KeyHelper.currentNumberKey(eventId);

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(redisLuaScripts.getLeaveScreenScript());
        script.setResultType(Long.class);

        return redisTemplate.execute(script, List.of(currentNumberKey));
    }


}
