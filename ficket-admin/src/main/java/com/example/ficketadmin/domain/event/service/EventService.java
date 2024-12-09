package com.example.ficketadmin.domain.event.service;

import com.example.ficketadmin.domain.event.dto.response.TemporaryUrlResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    @Value("${client.base-url}")
    private String baseUrl;

    private final StringRedisTemplate redisTemplate;
    private static final long EXPIRATION_TIME = 24 * 60 * 60; // 하루

    public TemporaryUrlResponse generateTemporaryUrl(Long eventId) {
        // Redis 키를 eventId 기반으로 설정
        String redisKey = "url:" + eventId;

        // 새로운 UUID 생성
        String uuid = UUID.randomUUID().toString();

        // Redis에 새로운 UUID 저장, 기존 키를 자동으로 덮어씀
        redisTemplate.opsForValue().set(redisKey, uuid, EXPIRATION_TIME, TimeUnit.SECONDS);

        // URL 구성
        String url = String.format("%s/events/%d/access?uuid=%s", baseUrl, eventId, uuid);
        log.info("생성된 URL: {}, 이벤트 ID: {}", url, eventId);

        return new TemporaryUrlResponse(url);
    }

}
