package com.example.ficketadmin.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling  // 스케줄링 활성화
@RequiredArgsConstructor
public class SchedulerConfig {

    private final JobLauncher jobLauncher;
    private final Job settlementJob;

    @Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시에 실행
    public void runBatchJob() {
        try {
            jobLauncher.run(settlementJob, new JobParameters()); // 배치 작업 실행
        } catch (JobExecutionException e) {
            log.error("Error executing batch job", e);
        }
    }

}
