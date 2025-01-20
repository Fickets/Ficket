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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 대기열 관리 서비스 클래스.
 * 대기열 진입, 상태 조회, 슬롯 관리 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QueueService {
    private static final Long WORKSPACE_TTL_SECONDS = 20 * 60L;
    private static final int INIT_SLOT_COUNT = 100;

    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final QueueProducer queueProducer;
    private final QueueMapper queueMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final EventServiceClient eventServiceClient;
    @Qualifier("queueReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> queueReactiveRedisTemplate;
    @Qualifier("redisTemplate")
    private final RedisTemplate<String, String> workRedisTemplate;
    @Qualifier("slotReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> slotReactiveRedisTemplate;

    /**
     * 최대 작업 가능한 슬롯 수 설정.
     *
     * @param eventId 이벤트 ID
     * @param max     최대 작업 가능한 슬롯 수
     */
    public Mono<Void> setMaxSlots(String eventId, int max) {
        String maxSlotKey = KeyHelper.getMaxSlotKey(eventId);
        return slotReactiveRedisTemplate.opsForValue()
                .set(maxSlotKey, String.valueOf(max))
                .then();
    }

    /**
     * 현재 작업 가능한 슬롯이 있는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 작업 가능한 슬롯이 있으면 true, 없으면 false 반환
     */
    public Mono<Boolean> canEnterQueue(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxKey = KeyHelper.getMaxSlotKey(eventId);

        return Mono.zip(
                        slotReactiveRedisTemplate.opsForValue().get(activeKey).defaultIfEmpty("0").map(Integer::parseInt),
                        slotReactiveRedisTemplate.opsForValue().get(maxKey).defaultIfEmpty("0").map(Integer::parseInt)
                ).map(tuple -> tuple.getT1() < tuple.getT2()) // active < max
                .doOnNext(canEnter -> log.info("슬롯 확인: eventId={}, canEnter={}", eventId, canEnter));
    }

    /**
     * 슬롯 점유.
     *
     * @param eventId 이벤트 ID
     * @param userId  사용자 ID
     * @return 점유 성공 시 true 반환
     */
    public Mono<Boolean> occupySlot(String userId, String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxKey = KeyHelper.getMaxSlotKey(eventId);

        return slotReactiveRedisTemplate.opsForValue()
                .increment(activeKey) // 현재 슬롯 증가
                .flatMap(active -> slotReactiveRedisTemplate.opsForValue().get(maxKey).map(Integer::parseInt)
                        .flatMap(max -> {
                            if (active > max) {
                                // 슬롯 초과 시 롤백
                                return slotReactiveRedisTemplate.opsForValue()
                                        .decrement(activeKey)
                                        .thenReturn(false);
                            }
                            // 작업 공간 생성
                            enterWorkSpace(userId, eventId);
                            return Mono.just(true);
                        }))
                .doOnNext(success -> log.info("슬롯 점유 결과: eventId={}, userId={}, success={}", eventId, userId, success));
    }

    /**
     * 슬롯 해제.
     *
     * @param eventId 이벤트 ID
     */
    public Mono<Void> releaseSlot(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);

        return slotReactiveRedisTemplate.opsForValue()
                .decrement(activeKey)
                .then();
    }

    /**
     * 슬롯 해제 (사용자 기반).
     *
     * @param userId 사용자 ID
     */
    public Mono<Void> releaseSlotByUserId(String userId) {
        String eventId = findEventIdByUserId(userId);

        if (eventId == null) {
            log.warn("사용자 {}의 eventId를 찾을 수 없습니다. 슬롯 해제를 건너뜁니다.", userId);
            return Mono.empty();
        }

        String activeKey = KeyHelper.getActiveSlotKey(eventId);

        return slotReactiveRedisTemplate.opsForValue()
                .decrement(activeKey)
                .then(Mono.fromRunnable(() -> {
                    String workspaceKey = KeyHelper.getFicketWorkSpace(eventId, userId);
                    workRedisTemplate.delete(workspaceKey);
                }));
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

        return queueReactiveRedisTemplate.opsForZSet()
                .size(redisKey)
                .flatMap(totalQueue -> queueReactiveRedisTemplate.opsForZSet()
                        .rank(redisKey, userId)
                        .map(rank -> {
                            long position = rank + 1; // 0-based index → 1-based index
                            QueueStatus status = position <= 1000 ? QueueStatus.ALMOST_DONE : QueueStatus.WAITING;

                            return queueMapper.toMyQueueStatusResponse(
                                    userId, eventId, position, totalQueue, status
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
        return queueReactiveRedisTemplate.opsForZSet()
                .remove(redisKey, userId)
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
    public Mono<Long> getUserPosition(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return queueReactiveRedisTemplate.opsForZSet()
                .rank(redisKey, userId)
                .defaultIfEmpty(-1L)
                .map(rank -> rank >= 0 ? rank + 1 : -1L); // 0-based index → 1-based index
    }

    /**
     * 대기열 크기 조회.
     *
     * @param eventId 이벤트 ID
     * @return 대기열 크기를 담은 Mono 객체
     */
    public Mono<Long> getQueueSize(String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return queueReactiveRedisTemplate.opsForZSet().size(redisKey);
    }

    /**
     * 대기열이 없고 슬롯에 여유가 있는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 즉시 입장 가능 여부를 나타내는 Mono<Boolean>
     */
    public Mono<Boolean> checkCanEnter(String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return queueReactiveRedisTemplate.opsForZSet()
                .size(redisKey)
                .publishOn(Schedulers.boundedElastic())
                .flatMap(queueSize -> {
                    if (queueSize == 0) {
                        return canEnterQueue(eventId); // canEnterQueue 호출
                    }
                    return Mono.just(false); // 대기열 크기가 0이 아니면 즉시 false 반환
                });
    }

    public Mono<Void> deleteSlot(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxKey = KeyHelper.getMaxSlotKey(eventId);

        return slotReactiveRedisTemplate.opsForValue().get(activeKey)
                .defaultIfEmpty("0")
                .zipWith(slotReactiveRedisTemplate.opsForValue().get(maxKey).defaultIfEmpty("0"))
                .flatMap(tuple -> {
                    long activeCount = Long.parseLong(tuple.getT1());
                    long maxCount = Long.parseLong(tuple.getT2());

                    if (activeCount == 0 && maxCount == INIT_SLOT_COUNT) {
                        return slotReactiveRedisTemplate.delete(activeKey)
                                .then(slotReactiveRedisTemplate.delete(maxKey)) // Mono<Long> -> Mono<Void>
                                .then(); // 최종적으로 Mono<Void> 반환
                    } else {
                        log.info("슬롯 삭제 조건 불일치: activeKey={}, activeCount={}, maxKey={}, maxCount={}",
                                activeKey, activeCount, maxKey, maxCount);
                        return Mono.empty(); // Mono<Void> 반환
                    }
                });
    }

    /**
     * 티켓팅 열기.
     */
    public void openTicketing() {
        List<Long> yesterdayOpenEvents = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getYesterdayOpenEventsCircuitBreaker",
                eventServiceClient::getYesterdayOpenEvents
        );

        for (Long yesterdayOpenEventId : yesterdayOpenEvents) {
            deleteSlot(String.valueOf(yesterdayOpenEventId))
                    .doOnSuccess(unused -> {
                        log.info("슬롯 삭제 eventId: {}", yesterdayOpenEventId);
                        String kafkaTopic = KeyHelper.getFicketKafkaQueue(String.valueOf(yesterdayOpenEventId));
                        log.info("카프카 토픽 삭제: {}", kafkaTopic);
                    })
                    .doOnError(error -> log.error("슬롯 삭제 실패: 이벤트 ID={}, 에러={}", yesterdayOpenEventId, error.getMessage()))
                    .subscribe();
        }

        List<Long> todayOpenEvents = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getTodayOpenEventsCircuitBreaker",
                eventServiceClient::getTodayOpenEvents
        );

        for (Long todayOpenEventId : todayOpenEvents) {
            String kafkaTopic = KeyHelper.getFicketKafkaQueue(String.valueOf(todayOpenEventId));
            kafkaTemplate.send(kafkaTopic, null);
            log.info("카프카 토픽 생성: {}", kafkaTopic);

            setMaxSlots(String.valueOf(todayOpenEventId), INIT_SLOT_COUNT) // 최대 슬롯 100 설정
                    .doOnSuccess(unused -> log.info("슬롯 초기화 완료: 이벤트 ID={}, 최대 슬롯={}", todayOpenEventId, INIT_SLOT_COUNT))
                    .doOnError(error -> log.error("슬롯 초기화 실패: 이벤트 ID={}, 에러={}", todayOpenEventId, error.getMessage()))
                    .subscribe();
        }
    }


    private void enterWorkSpace(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketWorkSpace(eventId, userId);
        workRedisTemplate.opsForValue().set(redisKey, "active", Duration.ofSeconds(WORKSPACE_TTL_SECONDS));
        log.info("사용자가 작업 공간에 진입했습니다: 사용자 ID={}, 이벤트 ID={}, TTL={}초",
                userId, eventId, WORKSPACE_TTL_SECONDS);
    }

    private String findEventIdByUserId(String userId) {
        // Redis SCAN 명령 실행
        Set<String> keys = workRedisTemplate.keys("ficket:workspace:*:" + userId);

        if (keys == null || keys.isEmpty()) {
            return null; // 키가 없으면 null 반환
        }

        // 단일 키에서 eventId 추출
        // 예: "ficket:workspace:eventId:userId" -> eventId 추출
        return keys.stream()
                .map(key -> key.split(":")[2]) // 세 번째 부분이 eventId
                .findFirst()
                .orElse(null);
    }
}
