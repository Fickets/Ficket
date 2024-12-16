package com.example.ficketevent.global.config.scheduler;

import com.example.ficketevent.domain.event.enums.Genre;
import com.example.ficketevent.domain.event.enums.Period;
import com.example.ficketevent.global.utils.RedisKeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class RankingResetScheduler {

    @Qualifier("rankingRedisTemplate")
    private final RedisTemplate<String, Object> redisTemplate;

    /**
     * 매일 오전 10시에 랭킹 초기화
     */
    @Scheduled(cron = "0 0 10 * * ?") // 매일 오전 10시
    public void resetRanking() {
        // Redis ZSet 초기화
        redisTemplate.delete(RedisKeyHelper.getViewRankingKey());
        log.info("Ranking scores reset at 10:00 AM");
    }

    /**
     * 매일 자정에 전주/전일 데이터를 삭제하고 복사
     */
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정
    public void archiveAndResetPreviousData() {

        Genre[] genres = Genre.values();

        for (Genre genre : genres) {
            // 전일 데이터 삭제 및 복사
            String dailyKey = RedisKeyHelper.getReservationKey(Period.DAILY, genre);
            String previousDailyKey = RedisKeyHelper.getReservationKey(Period.PREVIOUS_DAILY, genre);
            archiveAndReset(dailyKey, previousDailyKey);

            // 주간 데이터 삭제 및 복사
            String weeklyKey = RedisKeyHelper.getReservationKey(Period.WEEKLY, genre);
            String previousWeeklyKey = RedisKeyHelper.getReservationKey(Period.PREVIOUS_WEEKLY, genre);
            archiveAndReset(weeklyKey, previousWeeklyKey);
        }

        log.info("Archived and reset previous daily and weekly rankings at midnight");
    }

    /**
     * 데이터 복사 및 초기화
     *
     * @param sourceKey      복사할 원본 키
     * @param destinationKey 복사 대상 키
     */
    private void archiveAndReset(String sourceKey, String destinationKey) {
        // 원본 데이터 가져오기
        Set<ZSetOperations.TypedTuple<Object>> sourceData = redisTemplate.opsForZSet().rangeWithScores(sourceKey, 0, -1);

        // 원본 데이터를 대상 키로 복사
        if (sourceData != null && !sourceData.isEmpty()) {
            sourceData.forEach(tuple -> {
                redisTemplate.opsForZSet().add(destinationKey, tuple.getValue(), tuple.getScore());
            });
            log.info("Data copied from {} to {}", sourceKey, destinationKey);
        } else {
            log.warn("No data found for key: {}", sourceKey);
        }

        // 원본 데이터 초기화
        redisTemplate.delete(sourceKey);
        log.info("Data reset for key: {}", sourceKey);
    }
}
