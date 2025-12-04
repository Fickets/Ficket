package com.example.ficketevent.global.config.batch;

import com.example.ficketevent.domain.event.entity.FailedItem;
import com.example.ficketevent.domain.event.enums.JobStatus;
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
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.io.InputStream;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static com.example.ficketevent.global.result.error.ErrorCode.FAILED_ITEM_NOT_FOUND;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class EventToCsvBatchConfig {

    private static final String JOB_NAME = "eventToCsvJob";
    private static final String WORKER_STEP_NAME = "workerStep";
    private static final String RETRY_STEP_NAME = "retryFailedItemsStep";
    private static final int CHUNK_SIZE = 2000;
    private static final int PAGE_SIZE = 2000;

    private final CSVGenerator csvGenerator;
    private final EventRepository eventRepository;
    private final AwsS3Service awsS3Service;
    private final FailedItemRepository failedItemRepository;

    @Bean
    public Job eventToCsvJob(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new JobBuilder(JOB_NAME, jobRepository)
                .start(workerStep(jobRepository, transactionManager))
                .next(retryFailedItemsStep(jobRepository, transactionManager))
                .build();
    }

    @Bean
    public Step workerStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(WORKER_STEP_NAME, jobRepository)
                .<Long, Long>chunk(CHUNK_SIZE, transactionManager)
                .reader(cursorBasedEventReader())
                .writer(csvBatchWriter())
                .faultTolerant()
                .skip(Exception.class).skipLimit(5)
                .retry(Exception.class).retryLimit(3)
                .listener(workerSkipListener())
                .allowStartIfComplete(true)
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

                List<Long> nextBatch = eventRepository.findEventIdsByCursor(lastEventId, PAGE_SIZE);

                if (nextBatch.isEmpty()) {
                    return null;
                }

                lastEventId = nextBatch.get(nextBatch.size() - 1);
                currentBatchIterator = nextBatch.iterator();
                return currentBatchIterator.next();
            }
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> csvBatchWriter() {
        return items -> {
            List<Long> eventIds = (List<Long>) items.getItems();
            int itemCount = eventIds.size();

            String fileName = generateFileName();

            // CSV를 메모리에서 생성하고 InputStream으로 받기
            try (InputStream csvStream = csvGenerator.generateCsvStream(eventIds)) {
                // InputStream 크기 계산 (S3 메타데이터용)
                byte[] csvBytes = csvStream.readAllBytes();
                long contentLength = csvBytes.length;

                // 다시 InputStream 생성하여 S3 업로드
                try (InputStream uploadStream = new java.io.ByteArrayInputStream(csvBytes)) {
                    awsS3Service.uploadEventCsv(uploadStream, fileName, contentLength);
                }

                log.info("CSV 업로드 완료: {} ({} 건, {} bytes)", fileName, itemCount, contentLength);
            }
        };
    }

    private String generateFileName() {
        return String.format("events_%s_%s.csv",
                LocalDate.now(),
                UUID.randomUUID().toString().substring(0, 8));
    }

    @Bean
    public SkipListener<Long, Long> workerSkipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInWrite(Long item, Throwable t) {
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
    public Step retryFailedItemsStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        return new StepBuilder(RETRY_STEP_NAME, jobRepository)
                .<FailedItem, Long>chunk(100, transactionManager)
                .reader(failedItemReader())
                .processor(failedItemProcessor())
                .writer(failedItemWriter())
                .faultTolerant()
                .retry(Exception.class)
                .retryLimit(3)
                .listener(retrySkipListener())
                .build();
    }

    @Bean
    @StepScope
    public ItemReader<FailedItem> failedItemReader() {
        return new ItemReader<>() {
            private Iterator<FailedItem> failedItemIterator;
            private boolean initialized = false;

            @Override
            public FailedItem read() {
                if (!initialized) {
                    List<FailedItem> failedItems = failedItemRepository.findByStatus(JobStatus.PENDING);
                    failedItemIterator = failedItems.iterator();
                    initialized = true;
                    log.info("실패 항목 로드 완료: {}개", failedItems.size());
                }

                return failedItemIterator.hasNext() ? failedItemIterator.next() : null;
            }
        };
    }

    @Bean
    @StepScope
    public ItemProcessor<FailedItem, Long> failedItemProcessor() {
        return failedItem -> {
            if (failedItem.getItemId() == null) {
                throw new BusinessException(FAILED_ITEM_NOT_FOUND);
            }
            log.debug("실패한 항목 재처리 중: {}", failedItem.getItemId());
            return failedItem.getItemId();
        };
    }

    @Bean
    @StepScope
    public ItemWriter<Long> failedItemWriter() {
        return items -> {
            List<Long> itemIds = (List<Long>) items.getItems();
            List<FailedItem> failedItems = failedItemRepository.findAllByItemIdIn(itemIds);

            if (failedItems.isEmpty()) {
                log.warn("주어진 항목 ID에 해당하는 FailedItem이 없습니다: {}", itemIds);
                return;
            }

            failedItems.forEach(failedItem -> failedItem.setStatus(JobStatus.SUCCESS));
            failedItemRepository.saveAll(failedItems);
            log.info("{}개의 FailedItem이 SUCCESS 상태로 업데이트되었습니다.", failedItems.size());
        };
    }

    @Bean
    public SkipListener<FailedItem, Long> retrySkipListener() {
        return new SkipListener<>() {
            @Override
            public void onSkipInWrite(Long item, Throwable t) {
                log.error("재시도 중 쓰기 실패: {}, 사유: {}", item, t.getMessage());
            }

            @Override
            public void onSkipInRead(Throwable t) {
                log.error("재시도 중 읽기 실패: {}", t.getMessage());
            }

            @Override
            public void onSkipInProcess(FailedItem item, Throwable t) {
                log.error("재시도 중 처리 실패: {}, 사유: {}", item.getItemId(), t.getMessage());
            }
        };
    }
}