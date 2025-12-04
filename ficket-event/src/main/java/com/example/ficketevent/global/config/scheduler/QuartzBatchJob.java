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

    @Override
    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {

        try {
            String today = LocalDate.now().toString();
            JobParameters jobParameters = new JobParametersBuilder()
                    .addString("jobName", "eventToCsvJob")
                    .addString("executionDate", today)
                    .toJobParameters();

            jobLauncher.run(exportEventsJob, jobParameters);
            log.info("Batch Job 'exportEventsJob' successfully executed.");
        } catch (Exception e) {
            log.error("Batch execution error: {}", e.getMessage(), e);
            throw new JobExecutionException(e);
        }
    }
}
