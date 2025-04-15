package com.example.ficketevent.global.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.quartz.QuartzJobBean;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class QuartzBatchJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job exportEventsJob;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String INDEXING_LOCK = "FULL_INDEXING_LOCK";

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {
        boolean isLocked = Boolean.FALSE.equals(redisTemplate.opsForValue()
                .setIfAbsent(INDEXING_LOCK, "LOCKED", FULL_INDEX_TTL, TimeUnit.MILLISECONDS));

        if (isLocked) {
            log.warn("전체 인덱싱 작업이 이미 실행 중입니다. 실행을 건너뜁니다.");
            return;
        }

        try {
            String today = LocalDate.now().toString();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", "exportEventsJob")
                    .addString("executionDate", today)
                    .toJobParameters();

            jobLauncher.run(exportEventsJob, jobParameters);
            log.info("Batch Job 'exportEventsJob' successfully executed.");
        } catch (Exception e) {
            log.error("Batch execution error: {}", e.getMessage(), e);
            redisTemplate.delete(INDEXING_LOCK);
            throw new JobExecutionException(e);
        }
    }
}
