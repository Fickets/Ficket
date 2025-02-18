package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.dto.common.*;
import com.example.ficketevent.domain.event.dto.kafka.OrderDto;
import com.example.ficketevent.domain.event.dto.kafka.SeatMappingUpdatedEvent;
import com.example.ficketevent.domain.event.dto.request.SelectSeatInfo;
import com.example.ficketevent.domain.event.dto.response.*;
import com.example.ficketevent.domain.event.entity.*;
import com.example.ficketevent.domain.event.enums.Genre;
import com.example.ficketevent.domain.event.enums.Period;
import com.example.ficketevent.domain.event.mapper.StageSeatMapper;
import com.example.ficketevent.domain.event.messagequeue.SeatMappingProducer;
import com.example.ficketevent.domain.event.repository.EventScheduleRepository;
import com.example.ficketevent.domain.event.repository.EventStageRepository;
import com.example.ficketevent.domain.event.repository.SeatMappingRepository;
import com.example.ficketevent.domain.event.repository.StageSeatRepository;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.RedisKeyHelper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

import static com.example.ficketevent.domain.event.enums.Period.*;

/**
 * 행사장 좌석 정보를 관리하는 서비스 클래스.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StageSeatService {

    private final EventStageRepository eventStageRepository;
    private final StageSeatRepository stageSeatRepository;
    private final StageSeatMapper stageSeatMapper;
    private final SeatMappingRepository seatMappingRepository;
    private final RedissonClient redissonClient;
    private final SeatMappingProducer seatMappingProducer;
    private final EventScheduleRepository eventScheduleRepository;
    @Qualifier("rankingRedisTemplate") // 랭킹용 RedisTemplate
    private final RedisTemplate<String, Object> rankingRedisTemplate;

    /**
     * 주어진 행사장(stageId)에 대한 좌석 목록을 조회하고, 결과를 캐싱합니다.
     *
     * @param stageId 조회할 행사장의 ID
     * @return 좌석 목록을 포함한 StageSeatResponse 객체
     */
    @Cacheable(cacheNames = "stage-seats", key = "#stageId", unless = "#result == null")
    public StageSeatResponse getSeats(Long stageId) {
        // 행사장 ID를 통해 EventStage 엔티티 조회
        EventStage eventStage = findEventStageById(stageId);

        // 조회된 행사장의 좌석 정보를 DTO로 변환
        List<SeatResponse> seatDtos = mapSeatsToDto(eventStage);

        // 변환된 좌석 DTO 목록을 StageSeatResponse로 래핑하여 반환
        return new StageSeatResponse(seatDtos);
    }

    /**
     * 행사장 ID를 사용하여 EventStage 엔티티를 조회합니다.
     * 해당 ID에 대한 데이터가 없을 경우 BusinessException을 발생시킵니다.
     *
     * @param stageId 조회할 행사장 ID
     * @return 조회된 EventStage 엔티티
     * @throws BusinessException 행사장을 찾을 수 없는 경우 발생
     */
    private EventStage findEventStageById(Long stageId) {
        return eventStageRepository.findById(stageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
    }

    /**
     * 주어진 행사장에 대한 좌석 정보를 조회하고, 이를 DTO로 변환합니다.
     *
     * @param eventStage 조회할 행사장(EventStage 엔티티)
     * @return 좌석 정보가 담긴 SeatResponse DTO 리스트
     */
    private List<SeatResponse> mapSeatsToDto(EventStage eventStage) {
        return stageSeatRepository.findByEventStage(eventStage)
                .stream()
                .map(stageSeatMapper::toStageSeatDto) // StageSeat를 SeatResponse로 변환
                .toList();
    }

    /**
     * 좌석 조회
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 좌석 상태 목록
     */
    public List<SeatStatusResponse> getSeatStatusesByEventSchedule(Long eventScheduleId) {
        try {
            return getAvailableSeatsAsync(eventScheduleId);
        } catch (Exception e) {
            log.error("좌석 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new RuntimeException("좌석 조회 중 문제가 발생했습니다.");
        }
    }

    /**
     * 비동기 좌석 조회
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 좌석 상태 목록
     */
    private List<SeatStatusResponse> getAvailableSeatsAsync(Long eventScheduleId) throws ExecutionException, InterruptedException {
        // Redis 좌석 상태 비동기 조회
        CompletableFuture<Set<Long>> redisFuture = CompletableFuture.supplyAsync(() -> getLockedSeatFromRedis(eventScheduleId));

        // 데이터베이스 좌석 정보 비동기 조회
        CompletableFuture<List<SeatInfo>> dbFuture = CompletableFuture.supplyAsync(() -> seatMappingRepository.findSeatInfoByEventScheduleId(eventScheduleId));

        // 병렬 처리 결과 병합
        CompletableFuture.allOf(redisFuture, dbFuture).join();

        Set<Long> lockedSeats = redisFuture.get();
        List<SeatInfo> allSeats = dbFuture.get();

        // 좌석 상태 병합
        return allSeats.stream()
                .map(seat -> SeatStatusResponse.builder()
                        .seatMappingId(seat.getSeatMappingId())
                        .seatX(seat.getSeatX())
                        .seatY(seat.getSeatY())
                        .seatGrade(seat.getSeatGrade())
                        .seatRow(seat.getSeatRow())
                        .seatCol(seat.getSeatCol())
                        .status(determineSeatStatus(seat.getSeatMappingId(), lockedSeats, seat.getPurchased()))
                        .seatPrice(seat.getSeatPrice())
                        .build())
                .toList();
    }

    private Set<Long> getLockedSeatFromRedis(Long eventScheduleId) {
        Set<Long> lockedSeatSet = new HashSet<>();
        try {
            String seatKey = RedisKeyHelper.getSeatKey(eventScheduleId); // 키 생성
            RMap<String, String> seatStates = redissonClient.getMap(seatKey);

            for (String seatField : seatStates.keySet()) {
                try {
                    String seatIdStr = seatField.replace("seat_", "");
                    Long seatId = Long.parseLong(seatIdStr);
                    lockedSeatSet.add(seatId);
                } catch (NumberFormatException e) {
                    log.warn("Redis 해시 필드 '{}'에서 좌석 ID를 파싱하는 중 오류가 발생했습니다.", seatField, e);
                }
            }
        } catch (Exception e) {
            log.error("Redis에서 좌석 상태를 가져오는 중 오류 발생. EventScheduleId: {}", eventScheduleId, e);
            throw new RuntimeException("잠금된 좌석 데이터를 가져오는 중 오류 발생.", e);
        }

        log.info("EventScheduleId {}와 관련된 잠금된 좌석 수: {}", eventScheduleId, lockedSeatSet.size());
        return lockedSeatSet;
    }

    /**
     * 좌석 상태 결정
     *
     * @param seatMappingId 좌석 매핑 ID
     * @param lockedSeats   Redis에서 가져온 선점된 좌석 목록
     * @param purchased     구매 여부
     * @return 좌석 상태
     */
    private String determineSeatStatus(Long seatMappingId, Set<Long> lockedSeats, Boolean purchased) {
        if (Boolean.TRUE.equals(purchased)) {
            return "PURCHASED";
        } else if (lockedSeats.contains(seatMappingId)) {
            return "LOCKED";
        }
        return "AVAILABLE";
    }

    /**
     * 특정 이벤트 일정에 대한 등급별 남은 좌석 수를 조회합니다.
     *
     * @param eventScheduleId 이벤트 일정 ID
     * @return 등급별 남은 좌석 수 리스트
     */
    public List<SeatCntByGrade> getRemainingSeatsByGrade(Long eventScheduleId) {
        return seatMappingRepository.findPartitionSeatCounts(eventScheduleId);
    }

    public ReservedSeatsResponse getReservedSeats(Long userId, Long eventScheduleId) {
        String userKey = RedisKeyHelper.getUserKey(userId); // 키 생성
        RMap<String, String> userEvents = redissonClient.getMap(userKey);

        String eventField = "event_" + eventScheduleId;

        String value = userEvents.get(eventField);

        log.info("해당 유저가 선점한 좌석 : {}", value);

        if (value == null) {
            return ReservedSeatsResponse
                    .builder()
                    .reservedSeats(Collections.emptySet())
                    .build();
        }

        Set<Long> result = Arrays.stream(value.replaceAll("[\\[\\]]", "")
                        .split(","))
                .map(Long::valueOf)
                .collect(Collectors.toSet());

        return ReservedSeatsResponse
                .builder()
                .reservedSeats(result)
                .build();
    }

    @Transactional
    public void handleOrderCreatedEvent(OrderDto orderEvent) {
        log.info("Processing OrderCreatedEvent: {}", orderEvent);

        List<Long> successfullyUpdatedSeats = new ArrayList<>(); // 업데이트 성공한 좌석 추적

        try {
            List<Long> seatMappingIds = new ArrayList<>(orderEvent.getSeatMappingIds());
            Long ticketId = orderEvent.getTicketId();

            // SeatMapping 데이터베이스 조회 및 업데이트
            for (Long seatMappingId : seatMappingIds) {
                SeatMapping seatMapping = seatMappingRepository.findBySeatMappingIdAndTicketIdIsNull(seatMappingId)
                        .orElseThrow(() -> {
                            log.error("SeatMapping not found or already assigned. seatMappingId: {}", seatMappingId);
                            return new BusinessException(ErrorCode.SEAT_NOT_FOUND);
                        });

                seatMapping.setTicketId(ticketId);
                successfullyUpdatedSeats.add(seatMappingId); // 업데이트 성공한 좌석 ID 저장
            }

            // 랭킹 업데이트
            Event event = eventScheduleRepository.findEventByEventScheduleId(orderEvent.getEventScheduleId())
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
            updateReservationCount(event.getEventId(), event.getGenre(), seatMappingIds.size());

            log.info("OrderCreatedEvent processed successfully");

        } catch (BusinessException e) {
            log.error("Failed to process OrderCreatedEvent", e);

            // 성공적으로 업데이트된 좌석만 원복
            for (Long seatMappingId : successfullyUpdatedSeats) {
                SeatMapping seatMapping = seatMappingRepository.findById(seatMappingId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
                seatMapping.setTicketId(null); // ticketId를 NULL로 설정
            }

            // Redis 랭킹 데이터 복구 (랭킹이 업데이트된 경우에만)
            if (!successfullyUpdatedSeats.isEmpty()) {
                Event event = eventScheduleRepository.findEventByEventScheduleId(orderEvent.getEventScheduleId())
                        .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
                updateReservationCount(event.getEventId(), event.getGenre(), -successfullyUpdatedSeats.size());
            }

            // 실패 이벤트 발행
            Long orderId = orderEvent.getOrderId();
            SeatMappingUpdatedEvent failedEvent = new SeatMappingUpdatedEvent(orderId, false);
            seatMappingProducer.publishSeatMappingUpdatedEvent(failedEvent);
            log.info("Published failure event for orderId: {}", orderId);
        }
    }

    public ValidSeatInfoResponse validRequest(CreateOrderRequest createOrderRequest) {
        Long requestEventScheduleId = createOrderRequest.getEventScheduleId();

        EventSchedule eventSchedule = eventScheduleRepository.findById(requestEventScheduleId).orElseThrow(
                () -> new BusinessException(ErrorCode.EVENT_SESSION_NOT_FOUND)
        );

        Set<Long> seatMappingIds = createOrderRequest.getSelectSeatInfoList().stream().map(SelectSeatInfo::getSeatMappingId).collect(Collectors.toSet());

        Set<SelectSeatInfo> seatInfoInSeatMappingIds = seatMappingRepository.findSeatInfoInSeatMappingIds(seatMappingIds);

        return ValidSeatInfoResponse
                .builder()
                .reservationLimit(eventSchedule.getEvent().getReservationLimit())
                .selectSeatInfoList(seatInfoInSeatMappingIds)
                .build();
    }

    /**
     * 구매한 좌석 수만큼 예매 순위 업데이트
     *
     * @param eventId   구매한 eventId
     * @param seatCount 구매한 좌석 수
     */
    private void updateReservationCount(Long eventId, List<Genre> eventGenre, int seatCount) {
        for (Genre genre : eventGenre) {
            String reservationDailyKey = RedisKeyHelper.getReservationKey(DAILY, genre);
            rankingRedisTemplate.opsForZSet().incrementScore(reservationDailyKey, String.valueOf(eventId), seatCount);

            String reservationWeeklyKey = RedisKeyHelper.getReservationKey(WEEKLY, genre);
            rankingRedisTemplate.opsForZSet().incrementScore(reservationWeeklyKey, String.valueOf(eventId), seatCount);

            String reservationMonthlyKey = RedisKeyHelper.getReservationKey(MONTHLY, genre);
            rankingRedisTemplate.opsForZSet().incrementScore(reservationMonthlyKey, String.valueOf(eventId), seatCount);
        }
    }

    /**
     * @param ticketInfo 환불 티켓 정보
     */
    @Transactional
    public void openSeat(TicketInfo ticketInfo) {
        Long ticketId = ticketInfo.getTicketId();
        int seatCount = seatMappingRepository.countSeatMappingByTicketId(ticketId);
        Event event = eventScheduleRepository.findEventByEventScheduleId(ticketInfo.getEventScheduleId())
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        updateReservationCount(event.getEventId(), event.getGenre(), -seatCount);

        seatMappingRepository.openSeat(ticketId);
    }


    /**
     * 예약 제한과 이미 구매된 티켓 수를 기반으로 남은 구매 가능 티켓 수를 계산하는 메서드입니다.
     *
     * @param ticketDto 티켓 정보가 담긴 DTO
     *                  - ticketIds: 조회할 티켓 ID 목록
     *                  - eventScheduleId: 이벤트 스케줄 ID
     * @return 구매 가능 티켓 수 (예약 제한 수 - 이미 구매된 티켓 수)
     * 만약 구매 가능 수량이 없으면 0을 반환합니다.
     */
    public Integer getAvailableCount(TicketDto ticketDto) {
        // 입력받은 DTO에서 티켓 ID 목록과 이벤트 스케줄 ID를 추출
        List<Long> ticketIds = ticketDto.getTicketIds();
        Long eventScheduleId = ticketDto.getEventScheduleId();

        // 이벤트 스케줄에 설정된 예약 제한 수를 조회
        int reservationLimit = eventScheduleRepository.findReservationLimit(eventScheduleId);

        // 구매된 티켓 수를 저장할 변수 초기화
        int count;

        // 티켓 ID가 null이거나 비어 있는 경우 처리
        if (ticketIds == null || ticketIds.isEmpty()) {
            count = 0; // 티켓 ID가 없는 경우 구매된 티켓 수는 0으로 간주
        } else {
            // 티켓 ID 목록을 기준으로 이미 구매된 티켓 수를 조회
            count = seatMappingRepository.countPurchasedSeatsByTicketId(ticketIds);
        }

        // 예약 제한에서 이미 구매된 티켓 수를 차감하여 남은 구매 가능 티켓 수를 계산
        return reservationLimit - count;
    }

    public TicketSimpleInfo getTicketSimpleInfo(Long ticketId){
        List<SeatMapping> seatMappings = seatMappingRepository.findAllByTicketId(ticketId);
        if (!seatMappings.isEmpty()){
            SeatMapping seatMapping = seatMappings.get(0);
            EventSchedule eventSchedule = seatMapping.getEventSchedule();
            Event event = eventSchedule.getEvent();
            String title = event.getTitle();
            String stageName = event.getEventStage().getStageName();
            List<String> seatInfo = seatMappings.stream()
                    .map((seat) -> {
                        String row = seat.getStageSeat().getSeatRow();
                        String col = seat.getStageSeat().getSeatCol();
                        return (row + "열" + col + "번");
                    })
                    .toList();
            return TicketSimpleInfo.builder()
                    .seatLoc(seatInfo)
                    .eventTitle(title)
                    .stageName(stageName)
                    .build();
        }
        return TicketSimpleInfo.builder().build();
    }

    public Long ticketSeatCount(Long ticketId){
        List<SeatMapping> seatMappings = seatMappingRepository.findAllByTicketId(ticketId);
        return (long) seatMappings.size();
    }
}
