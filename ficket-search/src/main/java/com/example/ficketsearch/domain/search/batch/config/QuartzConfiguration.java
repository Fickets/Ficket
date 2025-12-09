package com.example.ficketsearch.domain.search.batch.config;

import com.example.ficketsearch.domain.search.batch.quartz.FullIndexingQuartzJob;
import lombok.extern.slf4j.Slf4j;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Quartz Scheduler 설정
 * 
 * Job과 Trigger를 정의하여 스케줄링 설정
 */
@Slf4j
@Configuration
public class QuartzConfiguration {

    /**
     * 전체 색인 Job 정의
     * 
     * JobDetail: Quartz가 실행할 Job의 메타데이터
     * - identity: Job의 고유 식별자 (이름, 그룹)
     * - durability: 트리거가 없어도 Job 정보를 유지할지 여부
     */
    @Bean
    public JobDetail fullIndexingJobDetail() {
        return JobBuilder.newJob(FullIndexingQuartzJob.class)
                .withIdentity("fullIndexingJob", "batch-jobs")
                .withDescription("매일 새벽 2시 Elasticsearch 전체 색인 작업")
                .storeDurably() // Job이 트리거가 없어도 저장소에 유지
                .build();
    }

    /**
     * 매일 새벽 2시 실행되는 크론 트리거
     * 
     * Cron 표현식 설명: "0 0 2 ? * *"
     * - 초: 0 (0초)
     * - 분: 0 (0분)
     * - 시: 2 (새벽 2시)
     * - 일: ? (매일, 요일과 함께 사용할 때는 ? 사용)
     * - 월: * (매월)
     * - 요일: * (모든 요일)
     * 
     * 결과: 매일 새벽 02:00:00에 실행 (Asia/Seoul 시간대)
     */
    @Bean
    public Trigger fullIndexingTrigger() {
        return TriggerBuilder.newTrigger()
                .forJob(fullIndexingJobDetail())
                .withIdentity("fullIndexingTrigger", "batch-triggers")
                .withDescription("매일 새벽 2시 실행 (Asia/Seoul)")
                .withSchedule(
                        CronScheduleBuilder.cronSchedule("0 0 2 ? * *")
                                .inTimeZone(java.util.TimeZone.getTimeZone("Asia/Seoul"))
                                .withMisfireHandlingInstructionDoNothing() // Misfire 시 건너뛰기
                )
                .build();
    }

}