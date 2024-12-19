package com.example.ficketqueue.queue.kafka;

import com.example.ficketqueue.global.utils.KeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class QueueConsumer {

    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;

    @org.springframework.kafka.annotation.KafkaListener(topicPattern = "ficket-queue-.*", groupId = "ticketing-group", concurrency = "3")
    public void consume(ConsumerRecord<String, String> record) {
        String topic = record.topic();
        String eventId = topic.replace("ficket-queue-", "");
        String userId = record.value();

        log.info("Kafka 메시지 수신 [Event: {}, User: {}]", eventId, userId);

        String redisKey = KeyHelper.getFicketRedisQueue(eventId);

        addToRedisWithRetry(redisKey, userId, eventId);
    }

    private void addToRedisWithRetry(String redisKey, String userId, String eventId) {
        reactiveRedisTemplate.opsForList()
                .rightPush(redisKey, userId)
                .doOnSuccess(count -> log.info("Redis에 대기열 추가 성공 [Event: {}, User: {}, 대기열 인원: {}]", eventId, userId, count))
                .doOnError(ex -> log.error("Redis에 대기열 추가 실패 [Event: {}, User: {}, Error: {}]", eventId, userId, ex.getMessage()))
                .retryWhen(Retry.backoff(3, Duration.ofSeconds(1)).maxBackoff(Duration.ofSeconds(10))) // 3회 재시도, 최대 10초 백오프
                .subscribe(
                        null,
                        ex -> log.error("Redis 재시도 실패: 대기열 추가 불가 [Event: {}, User: {}, Error: {}]", eventId, userId, ex.getMessage())
                );
    }
}
