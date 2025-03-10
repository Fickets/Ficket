package com.example.ficketadmin.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Slf4j
@Configuration
@EnableScheduling  // 스케줄링 활성화
@RequiredArgsConstructor
public class SchedulerConfig {

    private final JobLauncher jobLauncher;
    @Autowired
    private ApplicationContext applicationContext;


    @Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시에 실행
    public void runBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            Job job = applicationContext.getBean("settlementJob", Job.class);
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionException e) {
            log.error("Error executing batch job", e);
        }
    }

}
