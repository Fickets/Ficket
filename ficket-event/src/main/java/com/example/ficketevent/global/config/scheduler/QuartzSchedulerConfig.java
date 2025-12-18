package com.example.ficketevent.global.config.scheduler;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.quartz.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.scheduling.quartz.SpringBeanJobFactory;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Configuration
@RequiredArgsConstructor
public class QuartzSchedulerConfig {

    private final ApplicationContext applicationContext;
    private final DataSource dataSource;
    private final PlatformTransactionManager transactionManager;

    private static final String JOB_NAME = "eventToCsvJob";

    @Bean
    public SpringBeanJobFactory springBeanJobFactory() {
        AutowiringSpringBeanJobFactory jobFactory = new AutowiringSpringBeanJobFactory();
        jobFactory.setApplicationContext(applicationContext);
        return jobFactory;
    }

    @Bean
    public SchedulerFactoryBean schedulerFactoryBean(Trigger trigger, JobDetail jobDetail) {
        SchedulerFactoryBean schedulerFactory = new SchedulerFactoryBean();

        // DataSource 설정
        schedulerFactory.setDataSource(dataSource);
        schedulerFactory.setJobFactory(springBeanJobFactory());

        schedulerFactory.setTransactionManager(transactionManager);

        // JobDetail & Trigger 등록
        schedulerFactory.setJobDetails(jobDetail);
        schedulerFactory.setTriggers(trigger);

        return schedulerFactory;
    }

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
                .withSchedule(CronScheduleBuilder.cronSchedule("0 0 1 * * ?")) // 매일 새벽 1시 실행
                .build();
    }
}