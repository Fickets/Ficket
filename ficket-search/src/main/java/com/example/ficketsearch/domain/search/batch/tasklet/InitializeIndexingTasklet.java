package com.example.ficketsearch.domain.search.batch.tasklet;

import com.example.ficketsearch.domain.search.service.FullIndexingService;
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
public class InitializeIndexingTasklet implements Tasklet {

    private final FullIndexingService fullIndexingService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        log.info("인덱싱 초기화를 시작합니다...");
        
        try {
            // FullIndexingService의 initializeIndexing 메서드 호출
            // 1. S3 저장소 설정
            // 2. 기존 스냅샷 삭제
            // 3. 현재 상태를 스냅샷으로 S3에 저장
            // 4. 기존 데이터 삭제
            // 5. 인덱스 생성
            fullIndexingService.initializeIndexing();
            
            log.info("인덱싱 초기화가 완료되었습니다.");
            return RepeatStatus.FINISHED;
            
        } catch (Exception e) {
            log.error("인덱싱 초기화 중 오류 발생", e);
            
            // 실패 시 스냅샷 복원 시도
            log.info("오류 발생으로 인해 스냅샷 복원을 시도합니다...");
            try {
                fullIndexingService.restoreSnapshot();
                log.info("스냅샷 복원이 완료되었습니다.");
            } catch (Exception restoreException) {
                log.error("스냅샷 복원 실패", restoreException);
            }
            
            throw new RuntimeException("인덱싱 초기화 실패", e);
        }
    }
}