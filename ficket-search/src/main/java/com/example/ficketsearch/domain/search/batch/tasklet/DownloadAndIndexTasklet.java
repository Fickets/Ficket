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
public class DownloadAndIndexTasklet implements Tasklet {

    private final FullIndexingService fullIndexingService;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        log.info("CSV 파일 다운로드 및 Elasticsearch 색인을 시작합니다...");

        // Job Execution Context에서 CSV 파일 경로 가져오기
        String csvFiles = (String) chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .get("csvFiles");

        if (csvFiles == null || csvFiles.isEmpty()) {
            throw new IllegalStateException("CSV 파일 경로가 존재하지 않습니다.");
        }

        log.info("처리할 CSV 파일 경로: {}", csvFiles);

        try {
            long startTime = System.currentTimeMillis();

            // FullIndexingService의 handleFullIndexing 메서드 호출
            // 1. S3에서 CSV 파일 다운로드
            // 2. CSV를 JSON 스트림으로 변환
            // 3. Elasticsearch Bulk API로 색인
            fullIndexingService.handleFullIndexing(csvFiles);

            long endTime = System.currentTimeMillis();
            long duration = (endTime - startTime) / 1000;

            log.info("CSV 파일 다운로드 및 색인이 완료되었습니다. 소요 시간: {}초", duration);

            // 통계 정보 저장
            contribution.incrementWriteCount(1);
            chunkContext.getStepContext()
                    .getStepExecution()
                    .getJobExecution()
                    .getExecutionContext()
                    .put("indexingDuration", duration);

            return RepeatStatus.FINISHED;

        } catch (Exception e) {
            log.error("CSV 파일 다운로드 및 색인 중 오류 발생", e);

            // 실패 시 스냅샷 복원 시도
            log.info("오류 발생으로 인해 스냅샷 복원을 시도합니다...");
            try {
                fullIndexingService.restoreSnapshot();
                log.info("스냅샷 복원이 완료되었습니다.");
            } catch (Exception restoreException) {
                log.error("스냅샷 복원 실패", restoreException);
            }

            throw new RuntimeException("CSV 다운로드 및 색인 실패", e);
        }
    }
}