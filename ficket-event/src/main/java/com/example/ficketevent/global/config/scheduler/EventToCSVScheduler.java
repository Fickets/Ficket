package com.example.ficketevent.global.config.scheduler;

import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.enums.IndexingType;
import com.example.ficketevent.domain.event.enums.OperationType;
import com.example.ficketevent.domain.event.messagequeue.IndexingProducer;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.CSVGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventToCSVScheduler {

    private final CSVGenerator csvGenerator;
    private final EventRepository eventRepository;
    private final AwsS3Service awsS3Service;
    private final IndexingProducer indexingProducer;

    @Scheduled(cron = "0 0 2 * * ?") // 매일 새벽 2시에 실행
    public void exportEventsToS3() {
        try {
            // 1. MySQL 데이터 조회
            List<Event> events = eventRepository.findAll();

            // 2. CSV 파일 생성
            String filePath = "events.csv";
            File csvFile = csvGenerator.generateCsv(events, filePath);

            // 3. S3에 파일 업로드
            String eventListInfoFileUrl = awsS3Service.uploadEventListInfoFile(csvFile);

            // 4. 로컬 파일 삭제 (선택 사항)
            if (csvFile.exists()) {
                csvFile.delete();
            }

            // 5. Kafka 메시지 전송
            indexingProducer.sendIndexingMessage(IndexingType.FULL_INDEXING, eventListInfoFileUrl, OperationType.CREATE); // 전체 인덱싱 메시지 전송
            log.info("Kafka 메시지 전송 성공: CSV 파일 URL: {}", eventListInfoFileUrl);
        } catch (Exception e) {
            log.error("CSV 파일 생성 또는 S3 업로드 실패 : {}", e.getMessage());
        }
    }

    // 추후 삭제 예정 (테스트용)
    public void test() {
        try {
            // 1. MySQL 데이터 조회
            List<Event> events = eventRepository.findAll();

            // 2. CSV 파일 생성
            String filePath = "events.csv";
            File csvFile = csvGenerator.generateCsv(events, filePath);

            // 3. S3에 파일 업로드
            String eventListInfoFileUrl = awsS3Service.uploadEventListInfoFile(csvFile);

            // 4. 로컬 파일 삭제
            if (csvFile.exists()) {
                csvFile.delete();
            }

            // 5. Kafka 메시지 전송
            indexingProducer.sendIndexingMessage(IndexingType.FULL_INDEXING, eventListInfoFileUrl, OperationType.CREATE); // 전체 인덱싱 메시지 전송
            log.info("Kafka 메시지 전송 성공: CSV 파일 URL: {}", eventListInfoFileUrl);
        } catch (Exception e) {
            log.error("CSV 파일 생성 또는 S3 업로드 실패 : {}", e.getMessage());
        }
    }
}
