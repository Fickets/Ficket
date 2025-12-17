package com.example.ficketsearch.domain.search.batch.tasklet;

import com.example.ficketsearch.domain.search.service.IndexingLockService;
import com.example.ficketsearch.domain.search.service.KafkaControlService;
import com.example.ficketsearch.global.config.kafka.KafkaConstants;
import com.example.ficketsearch.global.config.redis.IndexingLockConstants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IndexLockTasklet implements Tasklet {

    private final IndexingLockService indexingLockService;
    private final KafkaControlService kafkaControlService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("전체 색인 Write Lock 획득 시도 (진행 중인 부분 색인이 있다면 완료될 때까지 대기)...");

        // 1. Kafka Consumer pause (새로운 부분 색인 메시지 수신 차단)
        kafkaControlService.pausePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
        log.info("Kafka Consumer pause 완료 (새로운 부분 색인 차단)");

        try {
            // 2. Write Lock 획득 시도 (진행 중인 부분 색인이 모두 끝날 때까지 자동 대기)
            boolean acquired = indexingLockService.acquireFullIndexingLock(IndexingLockConstants.FULL_INDEXING_WAIT_TIME_SEC.toLong(), IndexingLockConstants.FULL_INDEXING_LEASE_TIME_SEC.toLong());

            if (!acquired) {
                // 락 획득 실패 시 Kafka resume
                kafkaControlService.resumePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
                throw new IllegalStateException("전체 색인 락 획득 실패");
            }

            // 3. 락 획득 성공 기록
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("fullIndexingLockAcquired", true);

            log.info("전체 색인 락 획득 성공 → 전체 색인 시작 가능");
            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            // 예외 발생 시 Kafka resume
            kafkaControlService.resumePartialIndexing(KafkaConstants.PARTIAL_INDEXING_LISTENER.toString());
            log.error("전체 색인 락 획득 중 오류 발생", e);
            throw e;
        }
    }
}