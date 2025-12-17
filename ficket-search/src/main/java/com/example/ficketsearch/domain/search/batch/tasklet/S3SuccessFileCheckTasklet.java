package com.example.ficketsearch.domain.search.batch.tasklet;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.example.ficketsearch.global.config.awsS3.S3Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class S3SuccessFileCheckTasklet implements Tasklet {

    private final AmazonS3 amazonS3;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        // 오늘 날짜 기준 경로 생성
        LocalDate today = LocalDate.now();
        String datePath = today.format(S3Constants.DATE_FORMAT.toDateFormatter());
        String s3Prefix = String.format(S3Constants.FULL_INDEX_PREFIX.toString(), datePath);
        String successFilePath = s3Prefix + S3Constants.SUCCESS_FILE;

        log.info("S3 경로에서 _SUCCESS 파일을 확인합니다: {}", successFilePath);

        // _SUCCESS 파일 존재 여부 확인
        try {
            if (!amazonS3.doesObjectExist(S3Constants.BUCKET_NAME.toString(), successFilePath)) {
                throw new IllegalStateException(
                        String.format("_SUCCESS 파일이 존재하지 않습니다: %s. 데이터 준비가 완료되지 않았습니다.",
                                successFilePath));
            }
            log.info("_SUCCESS 파일이 존재합니다. CSV 파일 목록을 조회합니다.");
        } catch (Exception e) {
            throw new IllegalStateException("S3 _SUCCESS 파일 확인 중 오류 발생", e);
        }

        // CSV 파일 목록 조회
        List<String> csvFiles = listCsvFiles(s3Prefix);

        if (csvFiles.isEmpty()) {
            throw new IllegalStateException("CSV 파일이 존재하지 않습니다: " + s3Prefix);
        }

        log.info("발견된 CSV 파일 수: {}", csvFiles.size());
        csvFiles.forEach(file -> log.debug("- {}", file));

        // CSV 파일 경로를 Job Execution Context에 저장
        String csvFilesString = String.join(",", csvFiles);
        chunkContext.getStepContext()
                .getStepExecution()
                .getJobExecution()
                .getExecutionContext()
                .put("csvFiles", csvFilesString);

        return RepeatStatus.FINISHED;
    }

    private List<String> listCsvFiles(String prefix) {
        return amazonS3.listObjectsV2(S3Constants.BUCKET_NAME.toString(), prefix)
                .getObjectSummaries().stream()
                .map(S3ObjectSummary::getKey)
                .filter(key -> key.endsWith(S3Constants.CSV_EXTENSION.toString()))
                .map(key -> String.format("s3://%s/%s", S3Constants.BUCKET_NAME, key))
                .collect(Collectors.toList());
    }
}
