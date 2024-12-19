package com.example.ficketqueue.service;

import com.example.ficketqueue.dto.response.MyQueueStatusResponse;
import com.example.ficketqueue.enums.QueueStatus;
import com.example.ficketqueue.global.utils.KeyHelper;
import com.example.ficketqueue.kafka.QueueProducer;
import com.example.ficketqueue.mapper.QueueMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;


@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class QueueService {

    private final QueueProducer queueProducer;
    private final ReactiveRedisTemplate<String, String> reactiveRedisTemplate;
    private final QueueMapper queueMapper;

    public Mono<Void> enterQueue(String userId, String eventId) {
        return Mono.fromRunnable(() -> queueProducer.addQueue(userId, eventId));
    }

    public Mono<MyQueueStatusResponse> getMyQueueStatus(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);

        return reactiveRedisTemplate.opsForList()
                .size(redisKey) // Redis 리스트의 크기로 총 대기인원 확인
                .flatMap(totalQueue -> reactiveRedisTemplate.opsForList()
                        .indexOf(redisKey, userId)
                        .map(position -> {
                            long userPosition = position + 1;
                            QueueStatus status = userPosition <= 1000 ? QueueStatus.ALMOST_DONE : QueueStatus.WAITING;

                            // 사용자의 순서를 기반으로 응답 객체 생성
                            return queueMapper.toMyQueueStatusResponse(
                                    userId, eventId, userPosition, totalQueue, status
                            );
                        })
                        .defaultIfEmpty(new MyQueueStatusResponse(userId, eventId, -1L, totalQueue, QueueStatus.CANCELLED))
                );
    }

    /**
     * 대기열에서 나가기
     *
     * @param userId  대기열에서 나가려는 사용자 ID
     * @param eventId 대기열이 속한 이벤트 ID
     * @return Mono<Void> 성공 시 빈 Mono 반환
     */
    public Mono<Void> leaveQueue(String userId, String eventId) {
        String redisKey = KeyHelper.getFicketRedisQueue(eventId);
        return reactiveRedisTemplate.opsForList()
                .remove(redisKey, 1, userId) // Redis 리스트에서 사용자 제거
                .doOnNext(count -> {
                    if (count > 0) {
                        log.info("사용자가 대기열에서 제거되었습니다: userId={}, eventId={}", userId, eventId);
                    } else {
                        log.warn("대기열에서 사용자를 찾을 수 없습니다: userId={}, eventId={}", userId, eventId);
                    }
                })
                .then();
    }

    public void enterPageFromQueue(String userId, String eventId) {
        //todo 사용자를 예매 페이지 진입
    }


    public void openTicketing() {
        // TODO Fegin으로 어제 티켓팅 오픈한 토픽 삭제
        // TODO Fegin으로 오늘 티켓팅 예정인 List<eventId> 반환 후 토픽 생성
        log.info("openTicketing");
    }
}
