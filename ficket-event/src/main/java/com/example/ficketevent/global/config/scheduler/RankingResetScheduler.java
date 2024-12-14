package com.example.ficketevent.global.config.scheduler;

import com.example.ficketevent.global.utils.RedisKeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingResetScheduler {

    @Qualifier("rankingRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    @Scheduled(cron = "0 0 10 * * ?") // 매일 오전 10시
    public void resetRanking() {
        // Redis ZSet 초기화
        redisTemplate.delete(RedisKeyHelper.getViewRankingKey());
        log.info("Ranking scores reset at 10:00 AM");
    }
}