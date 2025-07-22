//package com.example.ficketadmin.global.config;
//
//
//import com.example.ficketadmin.domain.settlement.entity.Settlement;
//import com.example.ficketadmin.domain.settlement.entity.SettlementRecordTemp;
//import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
//import com.example.ficketadmin.domain.settlement.entity.SettlementTemp;
//import com.example.ficketadmin.domain.settlement.mapper.SettlementRecordTempMapper;
//import com.example.ficketadmin.domain.settlement.repository.SettlementRecordTempRepository;
//import com.example.ficketadmin.domain.settlement.repository.SettlementTempRepository;
//import jakarta.persistence.EntityManagerFactory;
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.springframework.batch.core.*;
//import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
//
//import org.springframework.batch.core.configuration.annotation.JobScope;
//import org.springframework.batch.core.configuration.annotation.StepScope;
//import org.springframework.batch.core.job.builder.JobBuilder;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobRepository;
//import org.springframework.batch.core.step.builder.StepBuilder;
//import org.springframework.batch.item.Chunk;
//import org.springframework.batch.item.ItemProcessor;
//import org.springframework.batch.item.ItemReader;
//import org.springframework.batch.item.ItemWriter;
//import org.springframework.batch.item.database.JpaPagingItemReader;
//
//
//import org.springframework.context.ApplicationContext;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.core.task.SimpleAsyncTaskExecutor;
//import org.springframework.core.task.TaskExecutor;
//import org.springframework.transaction.PlatformTransactionManager;
//
//import java.math.BigDecimal;
//import java.util.ArrayList;
//import java.util.List;
//
//
//@Slf4j
//@Configuration
//@EnableBatchProcessing
//@RequiredArgsConstructor
//public class SpringBatchConfig {
//
//    private final JobRepository jobRepository;
//    private final PlatformTransactionManager transactionManager;
//    private final EntityManagerFactory entityManagerFactory;
//    private final SettlementRecordTempRepository settlementRecordTempRepository;
//    private final SettlementTempRepository settlementTempRepository;
//    private final SettlementRecordTempMapper settlementRecordTempMapper;
//
//    private final JobLauncher jobLauncher;
//    private final ApplicationContext applicationContext;
//
//    private static final ThreadLocal<List<Settlement>> settlementList = ThreadLocal.withInitial(ArrayList::new);
//    private static final ThreadLocal<List<SettlementRecordTemp>> settlementRecordTempList = ThreadLocal.withInitial(ArrayList::new);
//
//    private Long THREAD_NUM;
//
//    @Bean
//    public TaskExecutor taskExecutor() {
//        SimpleAsyncTaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();
//        taskExecutor.setConcurrencyLimit(10);  // 최대 10개의 스레드로 처리
//        return taskExecutor;
//    }
//
//    public void runSettlementBatchJob() {
//        try {
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("timestamp", System.currentTimeMillis())
//                    .toJobParameters(); 
//            THREAD_NUM = 0L;
//            jobLauncher.run(job, jobParameters);
//        } catch (JobExecutionException e) {
//            log.error("Error executing batch job", e);
//        }
//    }
//
//
//    @Bean
//    @JobScope
//    public Job settlementJob() {
//        return new JobBuilder("settlementJob", jobRepository)
//                .start(firstStep())
//                .build();
//    }
//
//
//    @Bean
//    @StepScope
//    public Step firstStep() {
//        return new StepBuilder("sampleStep", jobRepository)
//                .<Settlement, SettlementTemp>chunk(1000, transactionManager)
//                .reader(settlementItemReader(entityManagerFactory))
//                .processor(settlementCombinedProcessor())
//                .writer(settlementItemWriter())
//                .taskExecutor(taskExecutor())
//                .build();
//    }
//
//
//    @Bean
//    @StepScope
//    public JpaPagingItemReader<Settlement> settlementItemReader(EntityManagerFactory entityManagerFactory) {
//        JpaPagingItemReader<Settlement> reader = new JpaPagingItemReader<>();
//
//        long currentTime = System.currentTimeMillis();  // 현재 시간 (밀리초 기준)
//        long twoHoursInMillis = 2 * 60 * 60 * 1000;  // 2시간을 밀리초로 변환
//        long fourHoursInMillis = 4 * 60 * 60 * 1000; // 4시간을 밀리초로 변환
//
//        long startTime;
//        long endTime;
//
//        if (THREAD_NUM < 8) {
//            // 2시간씩 8개 범위
//            startTime = currentTime - ((7 - THREAD_NUM) * twoHoursInMillis);  // 뒤에서부터 계산
//            endTime = startTime + twoHoursInMillis;
//        } else {
//            // 4시간씩 2개 범위
//            startTime = currentTime - ((9 - THREAD_NUM) * fourHoursInMillis);  // 뒤에서부터 계산
//            endTime = startTime + fourHoursInMillis;
//        }
//
//
//        String query = "SELECT s FROM Settlement s WHERE s.settlementStatus = 'UNSETTLED' AND s.settlementTime >= :startTime AND s.settlementTime < :endTime";
//        reader.setQueryString(query);
//        reader.setEntityManagerFactory(entityManagerFactory);
//        reader.setPageSize(1000);
//        THREAD_NUM = (THREAD_NUM + 1) % 10;
//        return reader;
//    }
//
//
//    @Bean
//    @StepScope
//    public ItemProcessor<Settlement, SettlementTemp> settlementCombinedProcessor() {
//        return settlement -> {
//            // SettlementRecordTemp 생성 또는 누적
//            SettlementRecordTemp tempRecord = settlementRecordTempRepository
//                    .findBySettlementRecordId(settlement.getSettlementId())
//                    .orElseGet(() -> settlementRecordTempMapper.toSettlementRecordTemp(settlement.getSettlementRecord()));
//
//            tempRecord.setTotalNetSupplyAmount(tempRecord.getTotalNetSupplyAmount().add(settlement.getNetSupplyAmount()));
//            tempRecord.setTotalVat(tempRecord.getTotalVat().add(settlement.getVat()));
//            tempRecord.setTotalSupplyAmount(tempRecord.getTotalSupplyAmount().add(settlement.getSupplyValue()));
//            tempRecord.setTotalServiceFee(tempRecord.getTotalServiceFee().add(settlement.getServiceFee()));
//            tempRecord.setTotalRefundValue(tempRecord.getTotalRefundValue().add(settlement.getRefundValue()));
//            tempRecord.setTotalSettlementValue(tempRecord.getTotalSettlementValue().add(settlement.getSettlementValue()));
//            tempRecord.setTotalSettlementValue(tempRecord.getTotalSettlementValue().subtract(settlement.getRefundValue()));
//            tempRecord.setSettlementStatus(SettlementStatus.SETTLEMENT);
//
//            settlementList.get().add(settlement);
//            settlementRecordTempList.get().add(tempRecord); // ThreadLocal<List<SettlementRecordTemp>>
//
//            return SettlementTemp.builder()
//                    .settlementId(settlement.getSettlementId())
//                    .netSupplyAmount(settlement.getNetSupplyAmount())
//                    .vat(settlement.getVat())
//                    .supplyValue(settlement.getSupplyValue())
//                    .serviceFee(settlement.getServiceFee())
//                    .refundValue(settlement.getRefundValue())
//                    .settlementValue(settlement.getSettlementValue())
//                    .settlementStatus(SettlementStatus.SETTLEMENT)
//                    .orderId(settlement.getOrderId())
//                    .settlementRecord(settlement.getSettlementRecord())
//                    .isSettled(false)
//                    .build();
//        };
//    }
//
//    @Bean
//    @StepScope
//    public ItemWriter<SettlementTemp> settlementItemWriter() {
//        return new ItemWriter<SettlementTemp>() {
//            @Override
//            public void write(@NotNull Chunk<? extends SettlementTemp> chunk) throws Exception {
//
//                settlementTempRepository.saveAll(chunk.getItems());
//                // SettlementRecordTemp 저장
//                List<SettlementRecordTemp> recordTempList = settlementRecordTempList.get();
//                if (recordTempList != null && !recordTempList.isEmpty()) {
//                    settlementRecordTempRepository.saveAll(recordTempList);
//                    recordTempList.clear(); // 메모리 정리
//                }
//
//                // Settlement 저장
//                List<Settlement> settlementOriginList = settlementList.get();
//                settlementList.get().clear();
//
//            }
//        };
//    }
//
//}
