package com.example.ficketevent.global.config.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;


@Slf4j
@Component
@RequiredArgsConstructor
public class EventToCSVScheduler{

    private final Job job;
    private final JobLauncher jobLauncher;


    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시에 실행
    public void exportEventsToS3() throws JobExecutionAlreadyRunningException, JobParametersInvalidException, JobRestartException, JobInstanceAlreadyCompleteException {

        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터 추가
                .toJobParameters();

        jobLauncher.run(job, jobParameters);

    }

    public void test() throws JobExecutionAlreadyRunningException, JobRestartException, JobInstanceAlreadyCompleteException, JobParametersInvalidException {
        JobParameters jobParameters = new JobParametersBuilder()
                .addLong("timestamp", System.currentTimeMillis()) // 유니크 파라미터 추가
                .toJobParameters();

        jobLauncher.run(job, jobParameters);
    }

}