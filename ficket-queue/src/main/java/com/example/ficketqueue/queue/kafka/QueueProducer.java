package com.example.ficketqueue.queue.kafka;

import com.example.ficketqueue.global.utils.KeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueProducer {

    private static final int MAX_RETRY_COUNT = 3;
    private static final long INITIAL_BACKOFF = 1; // 초기 백오프 시간 (초)

    private final KafkaTemplate<String, String> kafkaTemplate;

    // 비동기 백오프 스케줄러
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void addQueue(String userId, String eventId) {
        String topic = KeyHelper.getFicketKafkaQueue(eventId);
        sendMessageWithAsyncRetry(topic, userId, eventId, MAX_RETRY_COUNT, INITIAL_BACKOFF);
    }

    private void sendMessageWithAsyncRetry(String topic, String userId, String eventId, int retryCount, long backoff) {
        CompletableFuture<SendResult<String, String>> future = kafkaTemplate.send(topic, eventId, userId);

        future.whenComplete((result, ex) -> {
            if (ex == null) {
                // 메시지 전송 성공
                RecordMetadata metadata = result.getRecordMetadata();
                log.info("메시지 전송 성공 [Event: {}, User: {}, Partition: {}, Offset: {}]",
                        eventId, userId, metadata.partition(), metadata.offset());
            } else {
                // 메시지 전송 실패
                log.error("메시지 전송 실패 [Event: {}, User: {}, Error: {}]", eventId, userId, ex.getMessage());
                if (retryCount > 0) {
                    log.info("비동기 재시도 예정 ({}초 후) [남은 횟수: {}]", backoff, retryCount - 1);
                    scheduler.schedule(() -> sendMessageWithAsyncRetry(topic, userId, eventId, retryCount - 1, backoff * 2),
                            backoff, TimeUnit.SECONDS); // 지수 백오프
                } else {
                    log.error("재시도 횟수 초과. 메시지 전송 실패 [Event: {}, User: {}]", eventId, userId);
                }
            }
        });
    }
}
