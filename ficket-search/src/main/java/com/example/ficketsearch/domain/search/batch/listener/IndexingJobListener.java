package com.example.ficketsearch.domain.search.batch.listener;

import com.example.ficketsearch.domain.search.service.IndexingLockService;
import com.example.ficketsearch.domain.search.service.KafkaControlService;
import com.example.ficketsearch.global.config.kafka.KafkaConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import static com.example.ficketsearch.global.config.kafka.KafkaConstants.FULL_INDEXING_FINISHED;
import static com.example.ficketsearch.global.config.kafka.KafkaConstants.INDEXING_CONTROL_TOPIC;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexingJobListener implements JobExecutionListener {

    private final IndexingLockService indexingLockService;
    private final KafkaControlService kafkaControlService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info("전체 색인 Job 시작: {}", jobExecution.getJobInstance().getJobName());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("전체 색인 Job 종료: {} (상태: {})", 
                jobExecution.getJobInstance().getJobName(), 
                jobExecution.getStatus());

        // 실패, 중단, 예외 발생 등 모든 경우에 대한 정리 작업
        try {
            Boolean lockAcquired = (Boolean) jobExecution.getExecutionContext()
                    .get("fullIndexingLockAcquired");

            if (Boolean.TRUE.equals(lockAcquired)) {
                log.info("Job 종료 시 정리 작업 시작 (락 해제 및 Kafka resume)");

                // 1. Write Lock 해제
                indexingLockService.releaseFullIndexingLock();
                log.info("전체 색인 Write Lock 해제 완료");

                // 2. Kafka Consumer resume
                kafkaControlService.resumePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
                log.info("Kafka Consumer resume 완료");

                // 3. 전체 색인 완료 이벤트 발행 (성공한 경우에만)
                if (jobExecution.getStatus().isUnsuccessful()) {
                    log.warn("Job이 실패 상태로 종료되어 완료 이벤트 발행하지 않음");
                } else {
                    kafkaTemplate.send(INDEXING_CONTROL_TOPIC.toString(), FULL_INDEXING_FINISHED.toString());
                    log.info("전체 색인 완료 이벤트 발행 완료");
                }
            }

        } catch (Exception e) {
            log.error("Job 종료 시 정리 작업 중 오류 발생", e);
            
            // 정리 작업 실패 시에도 최소한 Kafka resume 시도
            try {
                kafkaControlService.resumePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
                log.info("오류 발생 시 Kafka resume 시도 완료");
            } catch (Exception resumeError) {
                log.error("Kafka resume 중 추가 오류 발생", resumeError);
            }
        }
    }
}