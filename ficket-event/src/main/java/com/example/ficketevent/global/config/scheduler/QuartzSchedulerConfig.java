package com.example.ficketevent.global.config.scheduler;

import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class QuartzSchedulerConfig {

    private static final String JOB_NAME = "eventToCSVJob";

    @Bean
    public JobDetail jobDetail() {
        return JobBuilder.newJob(QuartzBatchJob.class)
                .withIdentity(JOB_NAME)
                .storeDurably()
                .build();
    }

    @Bean
    public Trigger trigger() {
        return TriggerBuilder.newTrigger()
                .forJob(jobDetail())
                .withIdentity(JOB_NAME + "Trigger")
                .withSchedule(CronScheduleBuilder.cronSchedule("0 13 22 * * ?")) // 매일 새벽 2시 실행
                .build();
    }
}