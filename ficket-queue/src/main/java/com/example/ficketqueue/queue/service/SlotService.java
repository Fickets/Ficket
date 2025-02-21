package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.global.utils.LuaScriptLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;

/**
 * 작업 공간 관리 서비스 클래스.
 * 슬롯 점유, 해제 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SlotService {

    @Qualifier("queueReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> queueReactiveRedisTemplate;
    @Qualifier("workReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> workReactiveRedisTemplate;
    @Qualifier("slotReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> slotReactiveRedisTemplate;
    private final EventServiceClient eventServiceClient;

    private static final String OCCUPY_SLOT_SCRIPT = LuaScriptLoader.loadScript("lua/occupy_slot.lua");
    private static final String RELEASE_SLOT_SCRIPT = LuaScriptLoader.loadScript("lua/release_slot.lua");
    private static final String ENTER_WORKSPACE_SCRIPT = LuaScriptLoader.loadScript("lua/enter_workspace.lua");
    private static final String DELETE_WORKSPACE_SCRIPT = LuaScriptLoader.loadScript("lua/delete_workspace.lua");

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
                return enterWorkSpace(userId, eventId);
            }
            return Mono.just(false);

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

    public Mono<Void> releaseSlotByEventIdAndUserId(String eventId, String userId) {
        String workSpaceKey = KeyHelper.getFicketWorkSpace(eventId, userId);

        return releaseSlot(eventId)
                .then(deleteWorkSpace(workSpaceKey));
    }

    public Mono<Void> deleteWorkSpace(String workspaceKey) {
        return workReactiveRedisTemplate.execute(
                        new DefaultRedisScript<>(DELETE_WORKSPACE_SCRIPT, Boolean.class),
                        List.of(workspaceKey)
                )
                .doOnNext(success -> log.info("워크스페이스 삭제 성공 여부: workspaceKey={}, result={}", workspaceKey, success))
                .doOnError(error -> log.warn("워크스페이스 삭제 실패: workspaceKey={}, error={}", workspaceKey, error.getMessage()))
                .then();
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


    private Mono<Boolean> enterWorkSpace(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketWorkSpace(eventId, userId);
        DefaultRedisScript<Long> script = new DefaultRedisScript<>(ENTER_WORKSPACE_SCRIPT, Long.class);


        return workReactiveRedisTemplate.execute(script, Collections.singletonList(redisKey))
                .singleOrEmpty() // Flux<Long> -> Mono<Long>
                .map(result -> result != null && result == 1) // 결과 값이 1이면 true 반환
                .defaultIfEmpty(false); // 결과 값이 없으면 false 반환
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

    public Mono<Void> unLockSeats(String eventScheduleId, String userId) {
        return eventServiceClient.unLockSeatByEventScheduleIdAndUserId(eventScheduleId, userId);
    }

    public Mono<String> getUserIdBySeatLock(String eventScheduleId, String seatMappingId) {
        return eventServiceClient.getUserIdBySeatLock(eventScheduleId, seatMappingId);
    }
}
