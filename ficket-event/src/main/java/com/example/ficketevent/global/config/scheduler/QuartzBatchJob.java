package com.example.ficketevent.global.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
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

import java.util.concurrent.TimeUnit;

@Slf4j
@RequiredArgsConstructor
public class QuartzBatchJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job exportEventsJob;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String FULL_INDEXING_RESERVED = "FULL_INDEXING_RESERVED";

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {
        try {
            boolean isLocked = Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(FULL_INDEXING_RESERVED, "true", FULL_INDEX_TTL, TimeUnit.MILLISECONDS));

            if (!isLocked) {
                log.warn("Job is already running. Skipping execution.");
                return;
            }

            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", "exportEventsJob") // 동일 파라미터로 고정
                    .toJobParameters();

            jobLauncher.run(exportEventsJob, jobParameters);

            log.info("Batch Job 'exportEventsJob' successfully executed.");
        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
                 JobParametersInvalidException e) {
            log.error("Failed to execute Batch Job 'exportEventsJob': {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
