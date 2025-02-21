package com.example.ficketqueue.queue.service;

import com.example.ficketqueue.global.webclient.WebClientConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventServiceClient {

    private final WebClient webClient;
    private final WebClientConfig webClientConfig; // WebClientConfig에서 Eureka URL 가져옴

    public Mono<Void> unLockSeatByEventScheduleIdAndUserId(String eventScheduleId, String userId) {
        String eventServiceUrl = webClientConfig.getEventServiceUrl(); // Eureka에서 event-service의 실제 URL 가져오기

        log.info("eventServiceUrl: {}", eventServiceUrl);

        return webClient.delete()
                .uri(eventServiceUrl + "/api/v1/events/unlock-seats?eventScheduleId=" + eventScheduleId + "&userId=" + userId)
                .retrieve()
                .bodyToMono(Void.class)
                .doOnSuccess(unused -> log.info("좌석 해제 성공, eventScheduleId={}, userId={}", eventScheduleId, userId))
                .doOnError(error -> log.error("좌석 해제 실패: {}", error.getMessage()));
    }

    public Mono<String> getUserIdBySeatLock(String eventScheduleId, String seatMappingId) {
        String eventServiceUrl = webClientConfig.getEventServiceUrl(); // Eureka에서 event-service의 실제 URL 가져오기

        log.info("eventServiceUrl: {}", eventServiceUrl);

        return webClient.get()
                .uri(eventServiceUrl + "/api/v1/events/userId?eventScheduleId=" + eventScheduleId + "&seatMappingId=" + seatMappingId)
                .retrieve()
                .bodyToMono(String.class)
                .doOnSuccess(unused -> log.info("userId 조회 성공, eventScheduleId={}, seatMappingId={}", eventScheduleId, seatMappingId))
                .doOnError(error -> log.error("userId 조회 실패: {}", error.getMessage()));
    }
}
