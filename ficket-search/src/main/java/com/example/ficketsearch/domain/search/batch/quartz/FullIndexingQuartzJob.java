package com.example.ficketsearch.domain.search.batch.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.scheduling.quartz.QuartzJobBean;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class FullIndexingQuartzJob extends QuartzJobBean {

    private final JobLauncher jobLauncher;
    private final Job fullIndexingJob;
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    protected void executeInternal(JobExecutionContext context) throws JobExecutionException {
        LocalDateTime now = LocalDateTime.now();
        log.info("실행 시간: {}", now.format(FORMATTER));
        log.info("Fire Time: {}", context.getFireTime());
        log.info("Next Fire Time: {}", context.getNextFireTime());
        
        try {
            // JobParameters에 현재 시간을 포함하여 각 실행을 고유하게 만듦
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("time", System.currentTimeMillis())
                    .addString("executionTime", now.format(FORMATTER))
                    .addString("triggeredBy", "quartz")
                    .toJobParameters();
            
            jobLauncher.run(fullIndexingJob, jobParameters);
            
            log.info("=== Quartz: 전체 색인 배치 작업 완료 ===");
            
        } catch (Exception e) {
            log.error("Quartz: 전체 색인 배치 작업 실패", e);
            throw new JobExecutionException("전체 색인 배치 작업 실패", e);
        }
    }
}