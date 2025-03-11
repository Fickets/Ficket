package com.example.ficketadmin.global.config;


import com.example.ficketadmin.domain.settlement.entity.Settlement;
import com.example.ficketadmin.domain.settlement.entity.SettlementRecordTemp;
import com.example.ficketadmin.domain.settlement.entity.SettlementStatus;
import com.example.ficketadmin.domain.settlement.entity.SettlementTemp;
import com.example.ficketadmin.domain.settlement.mapper.SettlementRecordTempMapper;
import com.example.ficketadmin.domain.settlement.repository.SettlementRecordTempRepository;
import com.example.ficketadmin.domain.settlement.repository.SettlementTempRepository;
import jakarta.persistence.EntityManagerFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.batch.core.*;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;

import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.JpaPagingItemReader;


import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SpringBatchConfig {

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EntityManagerFactory entityManagerFactory;
    private final SettlementRecordTempRepository settlementRecordTempRepository;
    private final SettlementTempRepository settlementTempRepository;
    private final SettlementRecordTempMapper settlementRecordTempMapper;

    private final JobLauncher jobLauncher;

    private final ApplicationContext applicationContext;


    public List<Settlement> settlementList = new ArrayList<>();
    public List<SettlementRecordTemp> settlementRecordTempList = new ArrayList<>();

    public void runSettlementBatchJob() {
        try {
            JobParameters jobParameters = new JobParametersBuilder()
                    .addLong("timestamp", System.currentTimeMillis())
                    .toJobParameters();
            Job job = applicationContext.getBean("settlementJob", Job.class);
            jobLauncher.run(job, jobParameters);
        } catch (JobExecutionException e) {
            log.error("Error executing batch job", e);
        }
    }


    @Bean
    @JobScope
    public Job settlementJob() {
        return new JobBuilder("settlementJob", jobRepository)
                .start(firstStep())
                .next(secondStep())
                .build();
    }


    @Bean
    @StepScope
    public Step firstStep() {
        return new StepBuilder("sampleStep", jobRepository)
                .<Settlement, Settlement>chunk(1000, transactionManager)
                .reader(settlementItemReader(entityManagerFactory))
                .writer(settlementItemWriter())
                .build();
    }


    @Bean
    @StepScope
    public ItemWriter<Settlement> settlementItemWriter() {
        return settlements -> settlements.forEach(settlementList::add);
    }


    @Bean
    @StepScope
    public JpaPagingItemReader<Settlement> settlementItemReader(EntityManagerFactory entityManagerFactory) {
        JpaPagingItemReader<Settlement> reader = new JpaPagingItemReader<>();
        reader.setQueryString("SELECT s FROM Settlement s WHERE s.settlementStatus = 'UNSETTLED'");
        reader.setEntityManagerFactory(entityManagerFactory);
        reader.setPageSize(1000);
        return reader;
    }

    @Bean
    @StepScope
    public Step secondStep() {
        return new StepBuilder("secondStep", jobRepository)
                .<Settlement, SettlementTemp>chunk(20, transactionManager)  // Settlement에서 SettlementTemp로 변환
                .reader(new ItemReader<Settlement>() {
                    private int index = 0;

                    @Override
                    public Settlement read() throws Exception {
                        if (index < settlementList.size()) {
                            return settlementList.get(index++);  // settlementList에서 Settlement 객체를 하나씩 꺼냄
                        }
                        return null;  // 모든 데이터 처리 후 null 반환
                    }
                })
                .processor(new ItemProcessor<Settlement, SettlementTemp>() {
                    @Override
                    public SettlementTemp process(Settlement settlement) throws Exception {
                        
                        // SettlementRecordTemp 를 찾아서 갱신 없으면 만들어서 값 입력
                        SettlementRecordTemp tmpRecord = settlementRecordTempRepository
                                .findBySettlementRecordId(settlement.getSettlementId())
                                .orElseGet(() -> settlementRecordTempMapper.toSettlementRecordTemp(settlement.getSettlementRecord()));

                        tmpRecord.setTotalNetSupplyAmount(tmpRecord.getTotalNetSupplyAmount().add(settlement.getNetSupplyAmount()));
                        tmpRecord.setTotalVat(tmpRecord.getTotalVat().add(settlement.getVat()));
                        tmpRecord.setTotalSupplyAmount(tmpRecord.getTotalSupplyAmount().add(settlement.getSupplyValue()));
                        tmpRecord.setTotalServiceFee(tmpRecord.getTotalServiceFee().add(settlement.getServiceFee()));
                        tmpRecord.setTotalRefundValue(tmpRecord.getTotalRefundValue().add(settlement.getRefundValue()));
                        tmpRecord.setTotalSettlementValue(tmpRecord.getTotalSettlementValue().add(settlement.getSettlementValue()));
                        BigDecimal updatedSettlementValue = tmpRecord.getTotalSettlementValue().subtract(settlement.getRefundValue());
                        tmpRecord.setTotalSettlementValue(updatedSettlementValue);
                        tmpRecord.setSettlementStatus(SettlementStatus.SETTLEMENT);
                        settlementRecordTempList.add(tmpRecord);
                        
                        // Settlement을 SettlementTemp로 변환
                        return SettlementTemp.builder()
                                .settlementId(settlement.getSettlementId())
                                .netSupplyAmount(settlement.getNetSupplyAmount())
                                .vat(settlement.getVat())
                                .supplyValue(settlement.getSupplyValue())
                                .serviceFee(settlement.getServiceFee())
                                .refundValue(settlement.getRefundValue())
                                .settlementValue(settlement.getSettlementValue())
                                .settlementStatus(SettlementStatus.SETTLEMENT)
                                .orderId(settlement.getOrderId())
                                .settlementRecord(settlement.getSettlementRecord())
                                .isSettled(false)
                                .build();
                    }
                })
                .writer(new ItemWriter<SettlementTemp>() {
                    @Override
                    public void write(@NotNull Chunk<? extends SettlementTemp> chunk) throws Exception {
                        settlementTempRepository.saveAll(chunk.getItems());
                        settlementRecordTempRepository.saveAll(settlementRecordTempList);
                        settlementRecordTempList.clear();
                        settlementList.clear();

                    }
                })
                .build();
    }

}
