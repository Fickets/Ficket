package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.dto.IndexingKafkaMessage;
import com.example.ficketsearch.domain.search.service.IndexingService;
import com.example.ficketsearch.domain.search.service.LockingService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;


@Slf4j
@Service
@RequiredArgsConstructor
public class IndexingConsumer {

    private final LockingService lockingService;
    private final IndexingService indexingService;

    private static final String INDEXING_LOCK = "Full_Indexing_Lock";
    private static final long FULL_INDEX_TTL = 60 * 60 * 1000L; // 1시간
    private static final String FULL_INDEXING_RESERVED = "FULL_INDEXING_RESERVED";
    private static final BlockingQueue<IndexingKafkaMessage> queue = new LinkedBlockingQueue<>();

    @KafkaListener(
            topics = "full-indexing",
            groupId = "full-indexing-group", // 컨슈머 그룹 고정
            concurrency = "1"                // 단일 컨슈머 실행
    )
    public void handleFullIndexing(String message) {

        log.info("Kafka 메시지 수신: {}", message);
        processFullIndexing(message);
    }


    private void processFullIndexing(String message) {
        try {
            log.info("전체 색인 작업을 시작합니다. Message: {}", message);
            lockingService.executeWithLock(INDEXING_LOCK, FULL_INDEX_TTL, () -> {
                indexingService.handleFullIndexing(message);
                log.info("전체 색인 작업이 완료되었습니다. Message: {}", message);
            });

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("전체 색인 작업 중단: {}", e.getMessage(), e);
        } finally {
            lockingService.release(FULL_INDEXING_RESERVED);
            processQueuedMessages(); // 대기 메시지 처리 호출
        }
    }

    private void processQueuedMessages() {
        log.info("대기 중인 메시지 처리 시작...");
        while (!queue.isEmpty()) {
            IndexingKafkaMessage message = queue.poll();
            if (message != null) {
                processPartialIndexing(message);
            }
        }
        log.info("대기 중인 메시지 처리가 완료되었습니다.");
    }


    @KafkaListener(
            topics = "partial-indexing",
            groupId = "partial-indexing-group"
    )
    public void handlePartialIndexing(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            IndexingKafkaMessage indexingKafkaMessage = mapper.readValue(message, IndexingKafkaMessage.class);
            log.info("부분 색인 Kafka 메시지 수신: {}", indexingKafkaMessage);

            if (isFullIndexingInProgress()) {
                log.info("전체 색인 작업 진행 중. 부분 색인 메시지를 대기 큐로 추가: {}", indexingKafkaMessage);
                queue.add(indexingKafkaMessage); // 대기 큐에 추가
                return;
            }

            processPartialIndexing(indexingKafkaMessage); // 즉시 처리
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e);
            throw new RuntimeException("Kafka 메시지 처리 실패", e);
        }
    }

    private void processPartialIndexing(IndexingKafkaMessage message) {
        log.info("부분 색인 작업을 시작합니다. 작업 유형: {}", message.getOperationType());
        String operationType = message.getOperationType();
        switch (operationType) {
            case "CREATE":
                log.info("CREATE 작업을 처리 중입니다.");
                indexingService.handlePartialIndexingCreate((Map<String, Object>) message.getPayload(), operationType);
                break;

            case "UPDATE":
                log.info("UPDATE 작업을 처리 중입니다.");
                indexingService.handlePartialIndexingUpdate((Map<String, Object>) message.getPayload(), operationType);
                break;

            case "DELETE":
                log.info("DELETE 작업을 처리 중입니다.");
                indexingService.handlePartialIndexingDelete((String) message.getPayload(), operationType);
                break;

            default:
                log.warn("알 수 없는 작업 유형입니다: {}", operationType);
                break;
        }
        log.info("부분 색인 작업이 완료되었습니다. 작업 유형: {}", message.getOperationType());
    }

    private boolean isFullIndexingInProgress() {
        // 전체 색인 예약 상태 확인
        if (lockingService.isExist(FULL_INDEXING_RESERVED)) {
            return true;
        }

        // 전체 색인 실행 상태 확인 (선택 사항)
//        if (lockingService.isLock(INDEXING_LOCK)) {
//            return true;
//        }

        return false;
    }

}
