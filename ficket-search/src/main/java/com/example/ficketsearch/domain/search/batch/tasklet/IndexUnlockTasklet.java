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
public class IndexUnlockTasklet implements Tasklet {

    private final LockingService lockingService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("Redisson 기반 전체 색인 락 해제 시도...");

        try {
            // JobExecutionContext에서 락 획득 여부 확인
            Boolean lockAcquired = (Boolean) chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .get("lockAcquired");

            if (Boolean.TRUE.equals(lockAcquired)) {
                lockingService.unlock();
            } else {
                log.info("락을 획득하지 않았으므로 해제를 스킵합니다.");
            }

        } catch (Exception e) {
            log.error("전체 색인 락 해제 중 오류 발생", e);
        }

        return RepeatStatus.FINISHED;
    }
}
