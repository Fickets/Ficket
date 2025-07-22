//package com.example.ficketadmin.global.config;
//
//import lombok.RequiredArgsConstructor;
//
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.annotation.EnableScheduling;
//import org.springframework.scheduling.annotation.Scheduled;
//
//
//@Configuration
//@EnableScheduling
//@RequiredArgsConstructor
//public class SchedulerConfig {
//
//    private final SpringBatchConfig springBatchConfig;
//
//    @Scheduled(cron = "0 0 2 * * ?")  // 매일 새벽 2시에 실행
//    public void runJob(){
//        springBatchConfig.runSettlementBatchJob();
//    }
//
//}
