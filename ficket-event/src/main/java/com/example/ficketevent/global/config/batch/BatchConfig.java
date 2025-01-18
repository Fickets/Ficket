package com.example.ficketevent.global.config.batch;

import com.example.ficketevent.domain.event.enums.IndexingType;
import com.example.ficketevent.domain.event.enums.OperationType;
import com.example.ficketevent.domain.event.messagequeue.IndexingProducer;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.CSVGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.partition.support.Partitioner;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.*;
import org.springframework.batch.item.data.RepositoryItemReader;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private final CSVGenerator csvGenerator;
    private final EventRepository eventRepository;
    private final AwsS3Service awsS3Service;
    private final IndexingProducer indexingProducer;
    private final S3CleanupJobListener s3CleanupJobListener;

    @Bean
    public Job exportEventsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {

        return new JobBuilder("exportEventsJob", jobRepository)
                .listener(s3CleanupJobListener)
                .start(managerStep(jobRepository, taskExecutor(), transactionManager))
                .next(sendKafkaMessageStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step managerStep(JobRepository jobRepository, @Qualifier("taskExecutor") TaskExecutor taskExecutor, PlatformTransactionManager transactionManager) {
        return new StepBuilder("managerStep", jobRepository)
                .partitioner("workerStep", partitioner())
                .step(workerStep(jobRepository, transactionManager))
                .taskExecutor(taskExecutor)
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Partitioner partitioner() {
        return gridSize -> {
            Map<String, ExecutionContext> partitions = new HashMap<>();
            long minId = eventRepository.findMinId();
            long maxId = eventRepository.findMaxId();
            long chunkSize = 2000;
            long numberOfPartitions = (maxId - minId + chunkSize) / chunkSize;

            for (int i = 0; i < numberOfPartitions; i++) {
                long start = minId + (i * chunkSize);
                long end = Math.min(start + chunkSize - 1, maxId);

                ExecutionContext context = new ExecutionContext();
                context.putLong("minId", start);
                context.putLong("maxId", end);
                partitions.put("partition" + i, context);
            }
            return partitions;
        };
    }

    @Bean
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("workerStep", jobRepository)
                .<Long, Long>chunk(2000, transactionManager)
                .reader(eventReader(null, null))
                .processor(batchProcessor())
                .writer(csvBatchWriter())
                .faultTolerant()
                .skip(Exception.class)
                .skipLimit(5)
                .retry(Exception.class)
                .retryLimit(3)
                .listener(skipListener())
                .allowStartIfComplete(true)
                .build();
    }

    @Bean
    public Step sendKafkaMessageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder("sendKafkaMessageStep", jobRepository)
                .tasklet((contribution, chunkContext) -> {
                    // 모든 파일 링크 가져오기
                    List<String> filePaths = awsS3Service.getFiles();

                    // 쉼표(,)로 구분하여 메시지 생성
                    String message = String.join(",", filePaths);

                    // Kafka로 메시지 전송
                    indexingProducer.sendIndexingMessage(IndexingType.FULL_INDEXING, message, OperationType.CREATE);

                    log.info("Kafka 메시지 전송 완료: {}", message);
                    return RepeatStatus.FINISHED; // Tasklet 완료
                }, transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public RepositoryItemReader<Long> eventReader(@Value("#{stepExecutionContext['minId']}") Long minId, @Value("#{stepExecutionContext['maxId']}") Long maxId) {
        RepositoryItemReader<Long> reader = new RepositoryItemReader<>();
        reader.setRepository(eventRepository);
        reader.setMethodName("findEventIdsByRange");
        reader.setArguments(List.of(minId, maxId));
        reader.setPageSize(100);
        reader.setSort(Map.of("eventId", Sort.Direction.ASC));
        return reader;
    }

    @Bean
    @StepScope
    public ItemProcessor<Long, Long> batchProcessor() {
        return item -> item; // Pass-through processor
    }

    @Bean
    @StepScope
    public ItemWriter<Long> csvBatchWriter() {
        return items -> {
            String filePath = String.format("%s.csv", UUID.randomUUID());
            try {
                File csvFile = csvGenerator.generateCsv((List<Long>) items.getItems(), filePath);
                awsS3Service.uploadEventListInfoFile(csvFile);
                if (!csvFile.delete()) {
                    throw new RuntimeException("Failed to delete CSV file: " + csvFile.getAbsolutePath());
                }
            } catch (Exception e) {
                throw new RuntimeException("Error processing batch", e);
            }
        };
    }

    @Bean
    public SkipListener<Long, Long> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInRead(Throwable t) {
                log.info("Skipped during read: {}", t.getMessage());
            }

            @Override
            public void onSkipInWrite(Long item, Throwable t) {
                log.error("Skipped during write: {}, Error: {}", item, t.getMessage());
            }

            @Override
            public void onSkipInProcess(Long item, Throwable t) {
                log.error("Skipped during process: {}, Error: {}", item, t.getMessage());
            }
        };
    }

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int coreCount = Runtime.getRuntime().availableProcessors();
        executor.setCorePoolSize(coreCount);
        executor.setMaxPoolSize(coreCount * 2);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("Batch-Thread-");
        executor.initialize();
        return executor;
    }
}
