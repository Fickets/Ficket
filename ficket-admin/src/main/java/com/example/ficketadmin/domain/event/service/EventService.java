package com.example.ficketadmin.domain.event.service;

import com.example.ficketadmin.domain.admin.dto.common.AdminInfoDto;
import com.example.ficketadmin.domain.admin.entity.Role;
import com.example.ficketadmin.domain.event.client.EventServiceClient;
import com.example.ficketadmin.domain.event.client.SlotServiceClient;
import com.example.ficketadmin.domain.event.client.TicketingServiceClient;
import com.example.ficketadmin.domain.event.dto.response.DailyRevenueResponse;
import com.example.ficketadmin.domain.event.dto.response.DayCountResponse;
import com.example.ficketadmin.domain.event.dto.response.GuestTokenResponse;
import com.example.ficketadmin.domain.event.dto.response.TemporaryUrlResponse;
import com.example.ficketadmin.global.jwt.JwtUtils;
import com.example.ficketadmin.global.result.error.ErrorCode;
import com.example.ficketadmin.global.result.error.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    @Value("${client.base-url}")
    private String baseUrl; // 클라이언트 요청의 기본 URL

    private final StringRedisTemplate redisTemplate; // Redis와의 연동을 위한 템플릿
    private final EventServiceClient eventServiceClient; // 이벤트 서비스 클라이언트 (FeignClient)
    private final TicketingServiceClient ticketingServiceClient;
    private final SlotServiceClient slotServiceClient;
    private final JwtUtils jwtUtils;

    private static final long EXPIRATION_TIME = 24 * 60 * 60; // 하루 동안(초 단위) 만료 시간

    /**
     * 이벤트 ID를 기반으로 날짜별 수익 정보를 계산합니다.
     *
     * @param eventId 수익 정보를 계산할 이벤트의 ID
     * @return 날짜별 수익 정보를 담은 리스트 (각 날짜별로 수익 데이터 포함)
     */
    @CircuitBreaker(name = "calculateDailyRevenueCircuitBreaker")
    public List<DailyRevenueResponse> calculateDailyRevenue(Long eventId) {
        return eventServiceClient.calculateDailyRevenue(eventId);
    }

    /**
     * 이벤트 ID를 기반으로 요일별 카운트 정보를 계산합니다.
     *
     * @param eventId 카운트 정보를 계산할 이벤트의 ID
     * @return 요일별 카운트를 포함한 응답 객체 (월, 화, 수, 목, 금, 토, 일)
     */
    @CircuitBreaker(name = "calculateDayCountCircuitBreaker", fallbackMethod = "fallbackDayCountResponse")
    public DayCountResponse calculateDayCount(Long eventId) {
        return eventServiceClient.calculateDayCount(eventId);
    }

    private DayCountResponse fallbackDayCountResponse(Throwable throwable) {
        log.error("Fallback executed for. Reason: {}", throwable.getMessage());

        Map<String, Long> dayCountMap = new LinkedHashMap<>();

        dayCountMap.put("Monday", 0L);
        dayCountMap.put("Tuesday", 0L);
        dayCountMap.put("Wednesday", 0L);
        dayCountMap.put("Thursday", 0L);
        dayCountMap.put("Friday", 0L);
        dayCountMap.put("Saturday", 0L);
        dayCountMap.put("Sunday", 0L);

        return new DayCountResponse(dayCountMap);
    }

    /**
     * 이벤트 ID를 기반으로 임시 URL을 생성합니다.
     *
     * @param eventId 임시 URL을 생성할 이벤트의 ID
     * @return 생성된 임시 URL을 포함한 응답 객체
     */
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

        return new TemporaryUrlResponse(url); // URL 응답 객체 반환
    }

    /**
     * 해당 이벤트 ID의 임시 URL이 존재하는지 확인하고, 존재하면 URL을 반환합니다.
     *
     * @param eventId 확인할 이벤트 ID
     * @return 임시 URL이 존재하면 해당 URL을 포함한 응답 객체, 없으면 null을 반환
     */
    public TemporaryUrlResponse checkTemporaryUrlExists(Long eventId) {
        String redisKey = "url:" + eventId;
        String uuid = redisTemplate.opsForValue().get(redisKey);

        if (uuid == null) {
            log.info("임시 url이 존재하지 않습니다.");
            return new TemporaryUrlResponse(null); // 존재하지 않으면 null 반환
        }

        // URL 구성
        String url = String.format("%s/events/%d/access?uuid=%s", baseUrl, eventId, uuid);

        log.info("기존 임시 URL: {}, 이벤트 ID: {}", url, eventId);
        return new TemporaryUrlResponse(url);
    }

    public GuestTokenResponse checkUrl(Long eventId, String url) {
        String redisKey = "url:" + eventId;
        String redisUrl = redisTemplate.opsForValue().get(redisKey);
        if (redisUrl == null) {
            throw new BusinessException(ErrorCode.URL_NOT_FOUNT);
        }
        if (!redisUrl.equals(url)) {
            throw new BusinessException(ErrorCode.URL_NOT_FOUNT);
        }
        // admin 토큰 발급
        AdminInfoDto guest = AdminInfoDto.builder()
                .adminId(eventId)
                .name("GUEST")
                .role(Role.GUEST)
                .build();
        String guestToken = jwtUtils.createAccessToken(guest);

        return new GuestTokenResponse(guestToken);
    }


    public void initializeSlot(String eventId, int maxSlot) {
        slotServiceClient.setMaxSlot(eventId, maxSlot);
    }

    public void deleteSlot(String eventId) {
        slotServiceClient.deleteSlot(eventId);
    }
}
