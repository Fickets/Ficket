package com.example.ficketevent.global.config.batch;

import com.example.ficketevent.global.utils.AwsS3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class PrepareJobListener implements JobExecutionListener {

    private final AwsS3Service awsS3Service;
    private final RedisTemplate<String, String> redisTemplate;

    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String FULL_INDEXING_RESERVED = "FULL_INDEXING_RESERVED";

    @Override
    public void beforeJob(JobExecution jobExecution) {
        // Job이 처음 시작할 때만 S3 파일 삭제
        redisTemplate.opsForValue().set(FULL_INDEXING_RESERVED, "true", FULL_INDEX_TTL, TimeUnit.MILLISECONDS);
        awsS3Service.deleteAllFiles();
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        // 필요 시 Job 완료 후 추가 작업 처리
    }
}
