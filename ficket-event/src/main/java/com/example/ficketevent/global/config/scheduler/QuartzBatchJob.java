//package com.example.ficketevent.global.config.scheduler;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.jetbrains.annotations.NotNull;
//import org.quartz.JobExecutionContext;
//import org.quartz.JobExecutionException;
//import org.springframework.batch.core.Job;
//import org.springframework.batch.core.JobParameters;
//import org.springframework.batch.core.JobParametersBuilder;
//import org.springframework.batch.core.JobParametersInvalidException;
//import org.springframework.batch.core.launch.JobLauncher;
//import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
//import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
//import org.springframework.batch.core.repository.JobRestartException;
//import org.springframework.scheduling.quartz.QuartzJobBean;
//
//@Slf4j
//@RequiredArgsConstructor
//public class QuartzBatchJob extends QuartzJobBean {
//
//    private final JobLauncher jobLauncher;
//    private final Job exportEventsJob;
//
//    @Override
//    protected void executeInternal(@NotNull JobExecutionContext context) throws JobExecutionException {
//        try {
//            // JobParameters를 설정합니다.
//            JobParameters jobParameters = new JobParametersBuilder()
//                    .addLong("timestamp", System.currentTimeMillis()) // 고유 파라미터
//                    .toJobParameters();
//
//            // Batch Job 실행
//            jobLauncher.run(exportEventsJob, jobParameters);
//
//            log.info("Batch Job 'exportEventsJob' successfully executed.");
//        } catch (JobExecutionAlreadyRunningException | JobRestartException | JobInstanceAlreadyCompleteException |
//                 JobParametersInvalidException e) {
//            log.error("Failed to execute Batch Job 'exportEventsJob': {}", e.getMessage(), e);
//            throw new JobExecutionException(e);
//        }
//    }
//}
