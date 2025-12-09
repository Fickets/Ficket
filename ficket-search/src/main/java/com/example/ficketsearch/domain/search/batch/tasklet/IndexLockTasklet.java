package com.example.ficketsearch.domain.search.batch.tasklet;

import com.example.ficketsearch.domain.search.service.LockingService;
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

    private final LockingService lockingService;

    private static final long WAIT_TIME_SEC = 60;   // 락 대기 최대 60초
    private static final long LEASE_TIME_SEC = 7200; // 락 유지 2시간

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {

        log.info("Redisson 기반 인덱스 락 획득 시도...");

        boolean acquired = lockingService.tryLock(WAIT_TIME_SEC, LEASE_TIME_SEC);

        if (!acquired) {
            throw new IllegalStateException("전체 색인 락 획득 실패 (다른 서버에서 수행 중)");
        }

        // 락 획득 기록 저장
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("lockAcquired", true);

        log.info("전체 색인 락 획득 성공");
        return RepeatStatus.FINISHED;
    }
}
