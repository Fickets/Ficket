package com.example.ficketsearch.domain.search.messageQueue;

import com.example.ficketsearch.domain.search.dto.FullIndexingMessage;
import com.example.ficketsearch.domain.search.dto.PartialIndexingMessage;
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

    private static final String INDEXING_LOCK = "FULL_INDEXING_LOCK";
    private static final BlockingQueue<PartialIndexingMessage> queue = new LinkedBlockingQueue<>();

    @KafkaListener(
            topics = "full-indexing",
            groupId = "full-indexing-group", // 컨슈머 그룹 고정
            concurrency = "1"                // 단일 컨슈머 실행
    )
    public void handleFullIndexing(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FullIndexingMessage fullIndexingMessage = mapper.readValue(message, FullIndexingMessage.class);
            processFullIndexing(fullIndexingMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }


    private void processFullIndexing(FullIndexingMessage message) {
        log.info("전체 색인 작업을 시작합니다. Message: {}", message);
        indexingService.handleFullIndexing(message.getS3UrlList());

        if (message.isLastMessage()) {
            log.info("마지막 메세지 잆니다. Message: {}", message);
            lockingService.releaseLock(INDEXING_LOCK);
            processQueuedMessages();
        }

        log.info("전체 색인 작업이 완료되었습니다. Message: {}", message);
    }

    private void processQueuedMessages() {
        log.info("대기 중인 메시지 처리 시작...");
        while (!queue.isEmpty()) {
            PartialIndexingMessage message = queue.poll();
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
            PartialIndexingMessage partialIndexingMessage = mapper.readValue(message, PartialIndexingMessage.class);
            log.info("부분 색인 Kafka 메시지 수신: {}", partialIndexingMessage);

            if (isFullIndexingInProgress()) {
                log.info("전체 색인 작업 진행 중. 부분 색인 메시지를 대기 큐로 추가: {}", partialIndexingMessage);
                queue.add(partialIndexingMessage); // 대기 큐에 추가
                return;
            }

            processPartialIndexing(partialIndexingMessage); // 즉시 처리
        } catch (JsonProcessingException e) {
            log.error("Kafka 메시지 처리 중 오류 발생: {}", message, e);
            throw new RuntimeException("Kafka 메시지 처리 실패", e);
        }
    }

    private void processPartialIndexing(PartialIndexingMessage message) {
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

    // 전체 색인 실행 상태 확인
    private boolean isFullIndexingInProgress() {
        return (lockingService.isLockAcquired(INDEXING_LOCK));
    }

    @KafkaListener(
            topics = "full-indexing-dlq",
            groupId = "full-indexing-dlq-group", // 컨슈머 그룹 고정
            concurrency = "1"                // 단일 컨슈머 실행
    )
    public void handleFailFullIndexing(String message) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            FullIndexingMessage fullIndexingMessage = mapper.readValue(message, FullIndexingMessage.class);
            log.warn("DLQ 메세지 : {}", fullIndexingMessage);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
