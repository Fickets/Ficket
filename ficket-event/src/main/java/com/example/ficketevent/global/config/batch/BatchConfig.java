package com.example.ficketevent.global.config.batch;

import com.example.ficketevent.domain.event.entity.FailedItem;
import com.example.ficketevent.domain.event.enums.JobStatus;
import com.example.ficketevent.domain.event.messagequeue.FullIndexingProducer;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.example.ficketevent.domain.event.repository.FailedItemRepository;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.CSVGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.*;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.File;
import java.util.*;

import static com.example.ficketevent.global.result.error.ErrorCode.FAILED_ITEM_NOT_FOUND;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class BatchConfig {

    private static final String JOB_NAME = "exportEventsJob";
    private static final String WORKER_STEP_NAME = "workerStep";
    private static final String RETRY_STEP_NAME = "retryFailedItemsStep";
    private static final String KAFKA_STEP_NAME = "sendKafkaMessageStep";
    private static final int CHUNK_SIZE = 2000;
    private static final int PAGE_SIZE = 2000;
    private static final int BATCH_SIZE = 100;
    private static final int QUEUE_CAPACITY = 100;
    private static final String THREAD_NAME_PREFIX = "Batch-Thread-";

    private final CSVGenerator csvGenerator;
    private final EventRepository eventRepository;
    private final AwsS3Service awsS3Service;
    private final FullIndexingProducer fullIndexingProducer;
    private final PrepareJobListener prepareJobListener;
    private final FailedItemRepository failedItemRepository;

    @Bean
    public Job exportEventsJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .listener(prepareJobListener)
                .start(workerStep(jobRepository, transactionManager))
                .next(retryFailedItemsStep(jobRepository, transactionManager))
                .next(sendKafkaMessageStep(jobRepository, transactionManager))
                .build();
    }


    @Bean
    public Step retryFailedItemsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(RETRY_STEP_NAME, jobRepository)
                .<FailedItem, Long>chunk(100, transactionManager)
                .reader(failedItemReader())
                .processor(failedItemProcessor())
                .writer(itemWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .listener(skipListener())
                .build();
    }

    @Bean
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(WORKER_STEP_NAME, jobRepository)
                .<Long, Long>chunk(CHUNK_SIZE, transactionManager)
                .reader(cursorBasedEventReader())
                .processor(batchProcessor())
                .writer(csvBatchWriter())
                .faultTolerant()
                .skip(Exception.class).skipLimit(5)
                .retry(Exception.class).retryLimit(3)
                .listener(skipListener())
                .allowStartIfComplete(true)
                .build();
    }


    @Bean
    public Step sendKafkaMessageStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(KAFKA_STEP_NAME, jobRepository)
                .tasklet(sendKafkaMessageTasklet(), transactionManager)
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<Long> cursorBasedEventReader() {
        return new ItemReader<>() {

            private Long lastEventId = 0L;
            private Iterator<Long> currentBatchIterator = Collections.emptyIterator();

            @Override
            public Long read() throws Exception {
                if (currentBatchIterator.hasNext()) {
                    return currentBatchIterator.next();
                }

                // 다음 배치 조회
                List<Long> nextBatch = eventRepository.findEventIdsByCursor(lastEventId, PAGE_SIZE);

                if (nextBatch.isEmpty()) {
                    return null; // 더 이상 데이터 없음
                }

                // 마지막 이벤트 아이디 갱신
                lastEventId = nextBatch.get(nextBatch.size() - 1);

                currentBatchIterator = nextBatch.iterator();
                return currentBatchIterator.next();
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<Long, Long> batchProcessor() {
        return item -> item;
    }

    @Bean
    @StepScope
    public ItemWriter<Long> csvBatchWriter() {
        return items -> {
            String filePath = String.format("%s.csv", UUID.randomUUID());
            File csvFile = csvGenerator.generateCsv((List<Long>) items.getItems(), filePath);
            awsS3Service.uploadEventListInfoFile(csvFile);
            if (!csvFile.delete()) {
                throw new IllegalStateException("CSV 파일 삭제 실패: " + csvFile.getAbsolutePath());
            } else {
                log.info("CSV 파일이 성공적으로 삭제되었습니다: {}", csvFile.getAbsolutePath());
            }
        };
    }

    @Bean
    public Tasklet sendKafkaMessageTasklet() {
        return (contribution, chunkContext) -> {
            List<String> filePaths = awsS3Service.getFiles();

            int totalBatches = (int) Math.ceil((double) filePaths.size() / BATCH_SIZE);

            log.info("전체 CSV 파일 개수 : {}", totalBatches);

            for (int batchIndex = 0; batchIndex < totalBatches; batchIndex++) {
                int fromIndex = batchIndex * BATCH_SIZE;
                int toIndex = Math.min(fromIndex + BATCH_SIZE, filePaths.size());
                List<String> batch = filePaths.subList(fromIndex, toIndex);
                String message = String.join(",", batch);

                boolean isFirstMessage = (batchIndex == 0);
                boolean isLastMessage = (batchIndex == totalBatches - 1);

                fullIndexingProducer.sendIndexingMessage(message, isFirstMessage, isLastMessage);
            }

            return RepeatStatus.FINISHED;
        };
    }


    @Bean
    public SkipListener<Long, Long> skipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInWrite(Long item, Throwable t) {
                // 실패 항목을 DB에 기록
                failedItemRepository.save(FailedItem.builder()
                        .itemId(item)
                        .reason(t.getMessage())
                        .status(JobStatus.PENDING)
                        .build());
                log.error("쓰기 작업 중 실패한 항목: {}, 사유: {}", item, t.getMessage());
            }

            @Override
            public void onSkipInRead(Throwable t) {
                log.error("읽기 작업 중 실패 발생: {}", t.getMessage());
            }

            @Override
            public void onSkipInProcess(Long item, Throwable t) {
                log.error("처리 작업 중 실패한 항목: {}, 사유: {}", item, t.getMessage());
            }
        };
    }

    @Bean
    @StepScope
    public ItemReader<FailedItem> failedItemReader() {
        // 실패한 항목 데이터를 읽어오는 ItemReader
        return () -> {
            // PENDING 상태의 FailedItem을 조회
            List<FailedItem> failedItems = failedItemRepository.findByStatus(JobStatus.PENDING);
            // 항목이 없으면 null 반환
            return failedItems.isEmpty() ? null : failedItems.remove(0);
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<FailedItem, Long> failedItemProcessor() {
        // FailedItem을 처리하는 ItemProcessor
        return failedItem -> {
            // FailedItem에 itemId가 없으면 예외 발생
            if (failedItem.getItemId() == null) {
                throw new BusinessException(FAILED_ITEM_NOT_FOUND);
            }
            log.info("실패한 항목 처리 중: {}", failedItem);
            // 처리 결과로 itemId 반환
            return failedItem.getItemId();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> itemWriter() {
        // 처리 완료된 항목을 데이터베이스에 업데이트하는 ItemWriter
        return items -> {
            // 처리된 항목 ID 리스트 가져오기
            List<Long> itemIds = (List<Long>) items.getItems();
            // ID 리스트에 해당하는 FailedItem을 한 번에 조회
            List<FailedItem> failedItems = failedItemRepository.findAllByItemIdIn(itemIds);

            // 조회된 항목이 없으면 경고 로그를 남기고 종료
            if (failedItems.isEmpty()) {
                log.warn("주어진 항목 ID에 해당하는 FailedItem이 없습니다: {}", itemIds);
                return;
            }

            // 상태를 SUCCESS로 업데이트
            failedItems.forEach(failedItem -> failedItem.setStatus(JobStatus.SUCCESS));
            // 업데이트된 항목을 한 번에 저장
            failedItemRepository.saveAll(failedItems);
            log.info("총 {}개의 FailedItem이 SUCCESS 상태로 업데이트되었습니다.", failedItems.size());
        };
    }


    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        int coreCount = Runtime.getRuntime().availableProcessors(); // CPU 코어 수
        executor.setCorePoolSize(coreCount); // 기본 스레드 수
        executor.setMaxPoolSize(coreCount * 2); // 최대 스레드 수
        executor.setQueueCapacity(QUEUE_CAPACITY); // 큐 용량 증가
        executor.setThreadNamePrefix(THREAD_NAME_PREFIX);
        executor.setAwaitTerminationSeconds(60); // 종료 대기 시간 설정
        executor.initialize();
        return executor;
    }


}
