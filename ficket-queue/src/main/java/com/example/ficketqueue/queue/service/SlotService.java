package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.global.utils.KeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * 작업 공간 관리 서비스 클래스.
 * 슬롯 점유, 해제 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SlotService {
    private static final Long WORKSPACE_TTL_SECONDS = 20 * 60L;
    private static final String OCCUPY_SLOT_SCRIPT =
            "local active = redis.call('get', KEYS[1]) or '0'; " +
                    "local max = redis.call('get', KEYS[2]) or '0'; " +
                    "if tonumber(active) < tonumber(max) then " +
                    "    redis.call('incr', KEYS[1]); " +
                    "    return 1; " +
                    "else " +
                    "    return 0; " +
                    "end;";
    private static final String RELEASE_SLOT_SCRIPT =
            "local active = redis.call('get', KEYS[1]) or '0'; " +
                    "if tonumber(active) > 0 then " +
                    "    redis.call('decr', KEYS[1]); " +
                    "    return 1; " +
                    "else " +
                    "    return 0; " +
                    "end;";
    private static final String DELETE_WORKSPACE_SCRIPT =
            "local workspaceKey = KEYS[1]; " +
                    "local result = redis.call('del', workspaceKey); " +
                    "if result == 1 then " +
                    "    return 1; " + // 삭제 성공
                    "else " +
                    "    return 0; " + // 삭제 실패
                    "end;";


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
     * @param maxSlot 최대 작업 가능한 슬롯 수
     */
    public Mono<Void> setMaxSlot(String eventId, int maxSlot) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxSlotKey = KeyHelper.getMaxSlotKey(eventId);
        return slotReactiveRedisTemplate.opsForValue()
                .set(activeKey, "0") // 활성 슬롯 초기화
                .then(slotReactiveRedisTemplate.opsForValue()
                        .set(maxSlotKey, String.valueOf(maxSlot))).then(); // 최대 슬롯 설정
    }

    public Mono<Void> deleteSlot(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxKey = KeyHelper.getMaxSlotKey(eventId);

        return slotReactiveRedisTemplate.opsForValue().get(activeKey)
                .defaultIfEmpty("0")
                .flatMap(activeValue -> {
                    long activeCount = Long.parseLong(activeValue);
                    if (activeCount == 0) {
                        return slotReactiveRedisTemplate.delete(activeKey)
                                .then(slotReactiveRedisTemplate.delete(maxKey))
                                .then(); // 최종적으로 Mono<Void> 반환
                    } else {
                        String errorMessage = String.format("Active count is not zero. Deletion aborted. activeKey=%s, activeCount=%d", activeKey, activeCount);
                        log.error(errorMessage);
                        return Mono.error(new IllegalStateException(errorMessage)); // 오류 발생
                    }
                });
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

        return slotReactiveRedisTemplate.execute(
                new DefaultRedisScript<>(OCCUPY_SLOT_SCRIPT, Boolean.class),
                List.of(activeKey, maxKey)
        ).flatMap(success -> {
            if (success) {
                enterWorkSpace(userId, eventId);
                return Mono.just(true);
            } else {
                return Mono.just(false);
            }

        }).hasElements();
    }

    /**
     * 슬롯 해제.
     *
     * @param eventId 이벤트 ID
     */
    public Mono<Void> releaseSlot(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);

        return slotReactiveRedisTemplate.execute(
                new DefaultRedisScript<>(RELEASE_SLOT_SCRIPT, Boolean.class),
                List.of(activeKey)
        ).doOnNext(success -> {
            if (success) {
                log.info("슬롯 해제 성공: eventId={}", eventId);
            } else {
                log.warn("슬롯 해제 실패 (슬롯이 이미 0): eventId={}", eventId);
            }
        }).then();
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

        String workspaceKey = KeyHelper.getFicketWorkSpace(eventId, userId);

        return releaseSlot(eventId)
                .then(deleteWorkSpace(workspaceKey)) // Lua 스크립트를 사용해 워크스페이스 삭제
                .doOnSuccess(unused -> log.info("releaseSlotByUserId 완료: userId={}, eventId={}", userId, eventId))
                .doOnError(error -> log.error("releaseSlotByUserId 실패: userId={}, eventId={}, error={}", userId, eventId, error.getMessage()));
    }

    public Mono<Void> deleteWorkSpace(String workspaceKey) {
        return Mono.fromRunnable(() ->
                        workRedisTemplate.execute(
                                new DefaultRedisScript<>(DELETE_WORKSPACE_SCRIPT, Boolean.class),
                                List.of(workspaceKey)
                        )
                ).doOnSuccess(unused -> log.info("워크스페이스 삭제 완료: workspaceKey={}", workspaceKey))
                .doOnError(error -> log.warn("워크스페이스 삭제 실패: workspaceKey={}, error={}", workspaceKey, error.getMessage())).then();
    }


    /**
     * 작업 가능한 슬롯이 있는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 작업 가능한 슬롯이 있으면 true, 없으면 false 반환
     */
    public Mono<Boolean> hasAvailableSlot(String eventId) {
        String activeKey = KeyHelper.getActiveSlotKey(eventId);
        String maxKey = KeyHelper.getMaxSlotKey(eventId);

        return Mono.zip(
                slotReactiveRedisTemplate.opsForValue().get(activeKey).defaultIfEmpty("0").map(Integer::parseInt),
                slotReactiveRedisTemplate.opsForValue().get(maxKey).defaultIfEmpty("0").map(Integer::parseInt)
        ).map(tuple -> tuple.getT1() < tuple.getT2()); // active < max
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

    /**
     * 현재 이벤트의 사용 가능한 슬롯 수를 가져옵니다.
     *
     * @param eventId 이벤트 ID
     * @return 사용 가능한 슬롯 수
     */
    public Mono<Long> getAvailableSlots(String eventId) {
        String maxSlotKey = KeyHelper.getMaxSlotKey(eventId);
        String activeSlotKey = KeyHelper.getActiveSlotKey(eventId);
        String queueKey = KeyHelper.getFicketRedisQueue(eventId);

        Mono<Long> maxSlotsMono = slotReactiveRedisTemplate.opsForValue().get(maxSlotKey)
                .map(Long::parseLong)
                .defaultIfEmpty(0L);

        Mono<Long> activeSlotsMono = slotReactiveRedisTemplate.opsForValue().get(activeSlotKey)
                .map(Long::parseLong)
                .defaultIfEmpty(0L);

        Mono<Long> queueSizeMono = queueReactiveRedisTemplate.opsForZSet()
                .size(queueKey) // zset의 크기를 반환
                .defaultIfEmpty(0L);

        return Mono.zip(maxSlotsMono, activeSlotsMono, queueSizeMono)
                .map(tuple -> {
                    long maxSlots = tuple.getT1();
                    long activeSlots = tuple.getT2();
                    long queueSize = tuple.getT3();
                    long availableSlots = maxSlots - activeSlots;
                    return Math.min(availableSlots, queueSize); // 최소값 반환
                });
    }

}
