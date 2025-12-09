package com.example.ficketsearch.domain.search.batch.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Component
public class FullIndexingJobListener implements JobExecutionListener {

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("========================================");
        log.info("전체 색인 작업 시작");
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("시작 시간: {}", jobExecution.getStartTime());
        log.info("========================================");
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        LocalDateTime startTime = jobExecution.getStartTime();
        LocalDateTime endTime = jobExecution.getEndTime();
        BatchStatus status = jobExecution.getStatus();
        
        long durationSeconds = 0;
        if (startTime != null && endTime != null) {
            durationSeconds = Duration.between(startTime, endTime).getSeconds();
        }
        
        log.info("========================================");
        log.info("전체 색인 작업 완료");
        log.info("Job ID: {}", jobExecution.getJobId());
        log.info("상태: {}", status);
        log.info("시작 시간: {}", startTime);
        log.info("종료 시간: {}", endTime);
        log.info("소요 시간: {}초 ({}분)", durationSeconds, durationSeconds / 60);
        
        if (status == BatchStatus.COMPLETED) {
            Long indexingDuration = (Long) jobExecution.getExecutionContext().get("indexingDuration");
            if (indexingDuration != null) {
                log.info("색인 소요 시간: {}초", indexingDuration);
            }
            log.info("작업이 성공적으로 완료되었습니다.");
        } else if (status == BatchStatus.FAILED) {
            log.error("작업이 실패했습니다.");
            jobExecution.getAllFailureExceptions().forEach(throwable -> 
                log.error("실패 원인: {}", throwable.getMessage(), throwable)
            );
        }
        
        log.info("========================================");
    }
}