//package com.example.ficketevent.global.config.scheduler;
//
//import org.quartz.Scheduler;
//import org.quartz.spi.JobFactory;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.scheduling.quartz.SchedulerFactoryBean;
//
//@Configuration
//public class QuartzConfig {
//
//    @Bean
//    public SchedulerFactoryBean schedulerFactoryBean(JobFactory jobFactory) {
//        SchedulerFactoryBean factoryBean = new SchedulerFactoryBean();
//        factoryBean.setJobFactory(jobFactory); // Spring 컨텍스트에서 Job 빈을 관리하도록 설정
//        return factoryBean;
//    }
//
//    @Bean
//    public Scheduler scheduler(SchedulerFactoryBean schedulerFactoryBean) throws Exception {
//        return schedulerFactoryBean.getScheduler();
//    }
//}