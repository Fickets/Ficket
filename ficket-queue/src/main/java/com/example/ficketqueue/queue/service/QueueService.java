package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.global.utils.CircuitBreakerUtils;
import com.example.ficketqueue.queue.client.EventServiceClient;
import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.enums.QueueStatus;
import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.queue.kafka.QueueProducer;
import com.example.ficketqueue.queue.mapper.QueueMapper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 대기열 관리 서비스 클래스.
 * 대기열 진입, 상태 조회, 슬롯 관리 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QueueService {

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final QueueProducer queueProducer;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final QueueMapper queueMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventServiceClient eventServiceClient;

    /**
     * 현재 활성 슬롯 수.
     * 이벤트별로 작업 중인 슬롯 수를 관리합니다.
     */
    private final ConcurrentHashMap<String, Integer> activeSlots = new ConcurrentHashMap<>();

    /**
     * 최대 작업 가능한 슬롯 수.
     * 이벤트별로 최대 작업 가능한 슬롯 수를 저장합니다.
     */
    private final ConcurrentHashMap<String, Integer> maxSlots = new ConcurrentHashMap<>();

    /**
     * 최대 작업 가능한 슬롯 수 설정.
     *
     * @param eventId 이벤트 ID
     * @param max     최대 작업 가능한 슬롯 수
     */
    public void setMaxSlots(String eventId, int max) {
        maxSlots.put(eventId, max);
        activeSlots.putIfAbsent(eventId, 0);
    }

    /**
     * 현재 작업 가능한 슬롯이 있는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 작업 가능한 슬롯이 있으면 true, 없으면 false 반환
     */
    public boolean canEnterQueue(String eventId) {
        int max = maxSlots.getOrDefault(eventId, 0);
        int active = activeSlots.getOrDefault(eventId, 0);
        return active < max;
    }

    /**
     * 슬롯 점유.
     * 작업을 시작할 때 슬롯을 점유합니다.
     *
     * @param eventId 이벤트 ID
     * @return 슬롯 점유 성공 시 true, 실패 시 false 반환
     */
    public synchronized boolean occupySlot(String eventId) {
        if (canEnterQueue(eventId)) {
            activeSlots.put(eventId, activeSlots.getOrDefault(eventId, 0) + 1);
            return true;
        }
        return false;
    }

    /**
     * 슬롯 해제.
     * 작업이 끝난 후 슬롯을 해제합니다.
     *
     * @param eventId 이벤트 ID
     */
    public synchronized void releaseSlot(String eventId) {
        activeSlots.put(eventId, Math.max(0, activeSlots.getOrDefault(eventId, 0) - 1));
    }

    /**
     * 대기열에 사용자 추가.
     *
     * @param userId  사용자 ID
     * @param eventId 이벤트 ID
     * @return 빈 Mono 객체
     */
    public Mono<Void> enterQueue(String userId, String eventId) {
        return Mono.fromRunnable(() -> queueProducer.addQueue(userId, eventId));
    }

    /**
     * 대기열 상태 조회.
     * 사용자의 대기열 위치 및 상태를 조회합니다.
     *
     * @param userId  사용자 ID
     * @param eventId 이벤트 ID
     * @return 사용자의 대기열 상태를 담은 Mono 객체
     */
    public Mono<MyQueueStatusResponse> getMyQueueStatus(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);

        return reactiveRedisTemplate.opsForList()
                .size(redisKey)
                .flatMap(totalQueue -> reactiveRedisTemplate.opsForList()
                        .indexOf(redisKey, userId)
                        .map(position -> {
                            long userPosition = position + 1;
                            QueueStatus status = userPosition <= 1000 ? QueueStatus.ALMOST_DONE : QueueStatus.WAITING;

                            return queueMapper.toMyQueueStatusResponse(
                                    userId, eventId, userPosition, totalQueue, status
                            );
                        })
                        .defaultIfEmpty(new MyQueueStatusResponse(userId, eventId, -1L, totalQueue, QueueStatus.CANCELLED))
                );
    }

    /**
     * 대기열에서 사용자 제거.
     *
     * @param userId  사용자 ID
     * @param eventId 이벤트 ID
     * @return 성공 시 빈 Mono 객체 반환, 실패 시 에러 반환
     */
    public Mono<Void> leaveQueue(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return reactiveRedisTemplate.opsForList()
                .remove(redisKey, 1, userId)
                .flatMap(count -> {
                    if (count > 0) {
                        log.info("사용자가 대기열에서 제거되었습니다: userId={}, eventId={}", userId, eventId);
                        return Mono.empty();
                    } else {
                        log.warn("대기열에서 사용자를 찾을 수 없습니다: userId={}, eventId={}", userId, eventId);
                        return Mono.error(new IllegalStateException("사용자를 찾을 수 없습니다."));
                    }
                });
    }

    /**
     * 사용자의 대기열 위치 조회.
     *
     * @param eventId 이벤트 ID
     * @param userId  사용자 ID
     * @return 사용자 위치를 담은 Mono 객체
     */
    public Mono<Long> getUserPosition(String eventId, String userId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return reactiveRedisTemplate.opsForList()
                .indexOf(redisKey, userId)
                .defaultIfEmpty(-1L)
                .map(position -> position >= 0 ? position + 1 : -1L);
    }

    /**
     * 대기열 크기 조회.
     *
     * @param eventId 이벤트 ID
     * @return 대기열 크기를 담은 Mono 객체
     */
    public Mono<Long> getQueueSize(String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return reactiveRedisTemplate.opsForList().size(redisKey);
    }

    /**
     * 대기열이 없고 슬롯에 여유가 있는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 즉시 입장 가능 여부를 나타내는 Mono<Boolean>
     */
    public Mono<Boolean> checkCanEnter(String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return reactiveRedisTemplate.opsForList()
                .size(redisKey)
                .map(queueSize -> queueSize == 0 && canEnterQueue(eventId));
    }

    /**
     * 티켓팅 열기.
     */
    public void openTicketing() {
        List<Long> yesterdayOpenEvents = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getYesterdayOpenEventsCircuitBreaker",
                () -> eventServiceClient.getYesterdayOpenEvents()
        );

        for (Long yesterdayOpenEventId : yesterdayOpenEvents) {
            String kafkaTopic = KeyHelper.getFicketKafkaQueue(String.valueOf(yesterdayOpenEventId));
            kafkaTemplate.send("delete-topic", kafkaTopic);
            log.info("카프카 토픽 삭제: {}", kafkaTopic);
        }

        List<Long> todayOpenEvents = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getTodayOpenEventsCircuitBreaker",
                () -> eventServiceClient.getTodayOpenEvents()
        );

        for (Long todayOpenEventId : todayOpenEvents) {
            String kafkaTopic = KeyHelper.getFicketKafkaQueue(String.valueOf(todayOpenEventId));
            kafkaTemplate.send("create-topic", kafkaTopic);
            log.info("카프카 토픽 생성: {}", kafkaTopic);

            setMaxSlots(String.valueOf(todayOpenEventId), 100); // 예시로 최대 100 슬롯 설정
            log.info("슬롯 초기화 완료: 이벤트 ID={}, 최대 슬롯=100", todayOpenEventId);
        }
    }
}
