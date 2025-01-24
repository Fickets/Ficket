package com.example.ficketqueue.queue.service;


import com.example.ficketqueue.queue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.queue.enums.QueueStatus;
import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.queue.kafka.QueueProducer;
import com.example.ficketqueue.queue.mapper.QueueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;


/**
 * 대기열 관리 서비스 클래스.
 * 대기열 진입, 상태 조회 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QueueService {

    private final QueueProducer queueProducer;
    private final QueueMapper queueMapper;
    @Qualifier("queueReactiveRedisTemplate")
    private final ReactiveRedisTemplate<String, String> queueReactiveRedisTemplate;


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
                            QueueStatus status = position <= 100 ? QueueStatus.ALMOST_DONE : QueueStatus.WAITING;

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
                    } else {
                        log.warn("대기열에서 사용자를 찾을 수 없습니다: userId={}, eventId={}", userId, eventId);
                    }
                    return Mono.empty();
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
     * 대기중인 사람이 없는지 확인.
     *
     * @param eventId 이벤트 ID
     * @return 대기열 유무를 나타내는 Mono<Boolean>
     */
    public Mono<Boolean> isQueueEmpty(String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return queueReactiveRedisTemplate.opsForZSet()
                .size(redisKey)
                .publishOn(Schedulers.boundedElastic())
                .map(queueSize -> queueSize == 0); // 대기열 크기가 0이면 true, 아니면 false 반환
    }

    /**
     * 이벤트 대기열에서 다음 사용자를 가져옵니다.
     *
     * @param eventId 이벤트 ID
     * @return 대기열의 다음 사용자 ID
     */
    public Mono<String> getNextUserInQueue(String eventId) {
        String queueKey = KeyHelper.getFicketRedisQueue(eventId);

        return queueReactiveRedisTemplate.opsForZSet()
                .popMin(queueKey) // ZSet에서 가장 낮은 score의 요소를 가져오고 삭제
                .flatMap(tuple -> {
                    if (tuple == null || tuple.getValue() == null) {
                        log.warn("ZSet이 비어 있음: queueKey={}, eventId={}", queueKey, eventId);
                        return Mono.empty();
                    }
                    String userId = tuple.getValue(); // 가져온 사용자 ID
                    log.info("ZSet에서 사용자 가져오기 성공: userId={}, eventId={}", userId, eventId);
                    return Mono.just(userId); // 사용자 ID 반환
                })
                .doOnError(error -> log.error("ZSet 처리 중 오류 발생: queueKey={}, eventId={}, error={}", queueKey, eventId, error.getMessage()));
    }
}
