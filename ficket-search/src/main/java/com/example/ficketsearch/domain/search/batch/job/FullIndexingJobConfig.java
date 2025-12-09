package com.example.ficketsearch.domain.search.batch.job;

import com.example.ficketsearch.domain.search.batch.listener.FullIndexingJobListener;
import com.example.ficketsearch.domain.search.batch.tasklet.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class FullIndexingJobConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    
    private final IndexLockTasklet indexLockTasklet;
    private final S3SuccessFileCheckTasklet s3SuccessFileCheckTasklet;
    private final InitializeIndexingTasklet initializeIndexingTasklet;
    private final DownloadAndIndexTasklet downloadAndIndexTasklet;
    private final IndexUnlockTasklet indexUnlockTasklet;
    private final FullIndexingJobListener fullIndexingJobListener;

    @Bean
    public Job fullIndexingJob() {
        return new JobBuilder("fullIndexingJob", jobRepository)
                .listener(fullIndexingJobListener)
                .start(acquireLockStep())
                .next(checkSuccessFileStep())
                .next(initializeIndexingStep())
                .next(downloadAndIndexStep())
                .next(releaseLockStep())
                .build();
    }

    @Bean
    public Step acquireLockStep() {
        return new StepBuilder("acquireLockStep", jobRepository)
                .tasklet(indexLockTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step checkSuccessFileStep() {
        return new StepBuilder("checkSuccessFileStep", jobRepository)
                .tasklet(s3SuccessFileCheckTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step initializeIndexingStep() {
        return new StepBuilder("initializeIndexingStep", jobRepository)
                .tasklet(initializeIndexingTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step downloadAndIndexStep() {
        return new StepBuilder("downloadAndIndexStep", jobRepository)
                .tasklet(downloadAndIndexTasklet, transactionManager)
                .build();
    }

    @Bean
    public Step releaseLockStep() {
        return new StepBuilder("releaseLockStep", jobRepository)
                .tasklet(indexUnlockTasklet, transactionManager)
                .build();
    }
}