package com.example.ficketevent.global.config.batch;

import com.example.ficketevent.global.utils.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class S3CleanupJobListener implements JobExecutionListener {

    private final AwsS3Service awsS3Service;


    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Job이 처음 시작할 때만 S3 파일 삭제
        if (jobExecution.getStatus().isRunning()) {
            awsS3Service.deleteAllFiles();
        }
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // 필요 시 Job 완료 후 추가 작업 처리
    }
}
