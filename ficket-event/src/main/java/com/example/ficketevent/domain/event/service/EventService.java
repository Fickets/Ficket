package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.AdminServiceClient;
import com.example.ficketevent.domain.event.dto.common.*;
import com.example.ficketevent.domain.event.dto.request.*;
import com.example.ficketevent.domain.event.dto.response.*;
import com.example.ficketevent.domain.event.entity.*;
import com.example.ficketevent.domain.event.enums.Genre;
import com.example.ficketevent.domain.event.enums.Period;
import com.example.ficketevent.domain.event.mapper.EventMapper;
import com.example.ficketevent.domain.event.mapper.TicketMapper;
import com.example.ficketevent.domain.event.repository.*;
import com.example.ficketevent.global.common.Pair;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.FileUtils;
import com.example.ficketevent.global.utils.RedisKeyHelper;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

import static com.example.ficketevent.domain.event.enums.Period.*;
import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;
import static com.example.ficketevent.global.utils.CircuitBreakerUtils.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final AdminServiceClient adminServiceClient;
    private final EventMapper eventMapper;
    private final TicketMapper ticketMapper;
    private final EventRepository eventRepository;
    private final EventStageRepository eventStageRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final StagePartitionRepository stagePartitionRepository;
    private final StageSeatRepository stageSeatRepository;
    private final AwsS3Service awsS3Service;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final SeatMappingRepository seatMappingRepository;
    @Qualifier("redisTemplate") // 캐시용 RedisTemplate
    private final RedisTemplate<String, Object> redisTemplate;
    @Qualifier("rankingRedisTemplate") // 랭킹용 RedisTemplate
    private final RedisTemplate<String, Object> rankingRedisTemplate;


    /**
     * 이벤트 생성 메서드
     */
    @Transactional
    @CacheEvict(cacheNames = "searchEventScheduledOpen", allEntries = true)
    public void createEvent(Long adminId, EventCreateReq req, MultipartFile poster, MultipartFile banner) {

        // 1. 회사 및 공연장, 관리자 정보 조회
        CompanyResponse companyResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getCompanyCircuitBreaker",
                () -> adminServiceClient.getCompany(req.getCompanyId())
        );

        AdminDto adminResponse = executeWithCircuitBreaker(circuitBreakerRegistry,
                "getAdminCircuitBreaker",
                () -> adminServiceClient.getAdmin(adminId)
        );

        EventStage eventStage = findEventStageByStageId(req.getStageId());

        // 2. 이벤트 생성
        Event newEvent = createNewEvent(req, companyResponse, adminResponse, eventStage);

        // 3. 파티션 생성
        Map<String, StagePartition> partitionMap = createPartitions(req.getSeats(), newEvent);

        // 4. 스케줄 및 좌석 매핑 생성
        createSchedulesAndMappings(req.getEventDate(), req.getSeats(), newEvent, partitionMap);

        // 5. 이미지 생성 및 추가
        EventImage eventImage = createEventImage(poster, banner);
        newEvent.addEventImage(eventImage);

        // 6. 공연장과 이벤트 연결
        eventStage.getEvents().add(newEvent);

        // 7. 총 정산 테이블 만들기
        eventRepository.save(newEvent);
        executeWithCircuitBreaker(circuitBreakerRegistry,
                "createTotalSettlement",
                () -> adminServiceClient.createTotalSettlement(newEvent.getEventId()));

    }


    /**
     * 새로운 이벤트 생성
     */
    private Event createNewEvent(EventCreateReq req, CompanyResponse companyResponse, AdminDto adminResponse, EventStage eventStage) {
        Event event = eventMapper.eventDtoToEvent(req, companyResponse.getCompanyId(), adminResponse.getAdminId(), eventStage);
        event.addEventStage(eventStage);
        return event;
    }

    // 파티션 생성
    private Map<String, StagePartition> createPartitions(List<SeatDto> seatDtoList, Event newEvent) {
        Map<String, StagePartition> partitionMap = seatDtoList.stream()
                .collect(Collectors.toMap(
                        SeatDto::getGrade,
                        seatDto -> StagePartition.builder()
                                .event(newEvent)
                                .partitionName(seatDto.getGrade())
                                .partitionPrice(seatDto.getPrice())
                                .build()
                ));
        partitionMap.values().forEach(newEvent::addStagePartition);
        return partitionMap;
    }

    // 스케줄 및 좌석 매핑 생성
    private void createSchedulesAndMappings(List<EventDateDto> eventDateDtoList, List<SeatDto> seatDtoList, Event newEvent, Map<String, StagePartition> partitionMap) {
        eventDateDtoList.forEach(eventDateDto -> {
            eventDateDto.getSessions().forEach(sessionDto -> {
                EventSchedule eventSchedule = EventSchedule.builder()
                        .event(newEvent)
                        .round(sessionDto.getRound())
                        .eventDate(LocalDateTime.of(eventDateDto.getDate(), LocalTime.parse(sessionDto.getTime())))
                        .build();

                createSeatMappings(seatDtoList, eventSchedule, partitionMap);
                newEvent.addEventSchedule(eventSchedule);
            });
        });
    }

    // 좌석 매핑 생성
    private void createSeatMappings(List<SeatDto> seatDtoList, EventSchedule eventSchedule, Map<String, StagePartition> partitionMap) {
        seatDtoList.forEach(seatDto -> {
            StagePartition stagePartition = partitionMap.get(seatDto.getGrade());
            seatDto.getSeats().forEach(seatId -> {
                StageSeat stageSeat = stageSeatRepository.findById(seatId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

                SeatMapping seatMapping = SeatMapping.builder()
                        .eventSchedule(eventSchedule)
                        .stagePartition(stagePartition)
                        .stageSeat(stageSeat)
                        .build();

                stagePartition.addSeatMapping(seatMapping);
                eventSchedule.addSeatMapping(seatMapping);
            });
        });
    }


    /**
     * 이벤트 이미지를 생성합니다.
     */
    private EventImage createEventImage(MultipartFile poster, MultipartFile banner) {
        String posterOriginUrl = awsS3Service.uploadPosterOriginImage(poster);
        String bannerOriginUrl = awsS3Service.uploadBannerOriginImage(banner);

        String posterMobileUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_MOBILE_POSTER, FileUtils.extractFileName(posterOriginUrl));
        String posterPcUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER, FileUtils.extractFileName(posterOriginUrl));
        String posterPcMain1Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN1, FileUtils.extractFileName(posterOriginUrl));
        String posterPcMain2Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN2, FileUtils.extractFileName(posterOriginUrl));

        String bannerPcUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_BANNER, FileUtils.extractFileName(bannerOriginUrl));
        String bannerMobileUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_MOBILE_BANNER, FileUtils.extractFileName(bannerOriginUrl));

        return eventMapper.toEventImage(
                posterOriginUrl,
                bannerOriginUrl,
                posterMobileUrl,
                posterPcUrl,
                posterPcMain1Url,
                posterPcMain2Url,
                bannerPcUrl,
                bannerMobileUrl
        );
    }


    /**
     * 이벤트 업데이트 메서드
     */
    @Caching(evict = {
            @CacheEvict(cacheNames = "events", key = "#eventId"),
            @CacheEvict(cacheNames = "searchEventScheduledOpen", allEntries = true)
    })
    @Transactional
    public void updateEvent(Long eventId, Long adminId, EventUpdateReq req, MultipartFile poster, MultipartFile banner) {
        Event findEvent = findEventByEventId(eventId);

        // 1. 회사 정보 및 관리자 정보 업데이트
        updateCompanyInfo(req, findEvent);
        updateAdminInfo(findEvent, adminId);

        // 2. 스테이지 정보 업데이트
        updateStageInfo(req, findEvent);

        // 3. 이벤트 정보 업데이트
        findEvent.updatedEvent(req);

        // 4. 스케줄 및 좌석 매핑, 좌석 구분 업데이트
        updateEventScheduleAndSeatMappingAndPartition(req, findEvent);

        // 5. 이미지 업데이트
        updateEventImages(findEvent, poster, banner);

        // 6. 수동 캐시 삭제
        deleteCache(eventId);
    }

    // 회사 정보 업데이트
    private void updateCompanyInfo(EventUpdateReq req, Event findEvent) {
        if (req.getCompanyId() != null) {
            CompanyResponse companyResponse = adminServiceClient.getCompany(req.getCompanyId());
            findEvent.setCompanyId(companyResponse.getCompanyId());
        }
    }

    private void updateAdminInfo(Event findEvent, Long adminId) {
        if (!findEvent.getAdminId().equals(adminId)) {
            AdminDto adminResponse = adminServiceClient.getAdmin(adminId);
            findEvent.setAdminId(adminResponse.getAdminId());
        }
    }

    // 스테이지 정보 업데이트
    private void updateStageInfo(EventUpdateReq req, Event findEvent) {
        if (req.getStageId() != null) {
            EventStage eventStage = findEventStageByStageId(req.getStageId());
            findEvent.setEventStage(eventStage);
        }
    }

    private void updateEventScheduleAndSeatMappingAndPartition(EventUpdateReq req, Event findEvent) {
        List<EventDateDto> updateEventSchedule = req.getEventDate();
        if (updateEventSchedule != null) {
            if (updateEventSchedule.isEmpty()) {
                throw new BusinessException(ErrorCode.EMPTY_EVENT_SCHEDULE);
            }

            List<EventSchedule> eventScheduleList = eventScheduleRepository.findEventScheduleByEvent(findEvent);

            // 요청과 기존 db에 있는것과 동일한 경우
            List<EventSchedule> schedulesToKeep = eventScheduleList.stream()
                    .filter(eventSchedule ->
                            updateEventSchedule.stream().anyMatch(eventDateDto ->
                                    eventDateDto.toLocalDateTimes().contains(eventSchedule.getEventDate())
                            )
                    )
                    .toList();

            // 요청에만 있는 추가된 일정
            List<EventDateDto> schedulesToAdd = updateEventSchedule.stream()
                    .filter(eventDateDto ->
                            eventDateDto.toLocalDateTimes().stream()
                                    .noneMatch(localDateTime ->
                                            eventScheduleList.stream()
                                                    .anyMatch(eventSchedule ->
                                                            localDateTime.isEqual(eventSchedule.getEventDate())
                                                    )
                                    )
                    )
                    .toList();

            // 기존 DB에만 있는 삭제할 일정
            List<EventSchedule> schedulesToDelete = eventScheduleList.stream()
                    .filter(eventSchedule ->
                            updateEventSchedule.stream()
                                    .noneMatch(eventDateDto ->
                                            eventDateDto.toLocalDateTimes().contains(eventSchedule.getEventDate())
                                    )
                    )
                    .toList();


            processSchedules(schedulesToKeep, schedulesToAdd, schedulesToDelete, req.getSeats(), findEvent);
        }
    }

    private void processSchedules(List<EventSchedule> schedulesToKeep, List<EventDateDto> schedulesToAdd, List<EventSchedule> schedulesToDelete, List<SeatDto> seatDtoList, Event event) {
        Map<String, StagePartition> partitionMap = Map.of();

        if (seatDtoList != null && !seatDtoList.isEmpty()) {
            // 1. 기존 파티션 삭제
            stagePartitionRepository.deleteByEvent(event);

            // 2. 새로운 파티션 생성
            partitionMap = createPartitions(seatDtoList, event);

            // 3. 유지할 스케줄 처리
            for (EventSchedule toKeep : schedulesToKeep) {
                createSeatMappings(seatDtoList, toKeep, partitionMap);
                event.addEventSchedule(toKeep);
            }

            // 4. 추가된 스케줄 처리
            for (EventDateDto eventDateDto : schedulesToAdd) {
                for (SessionDto sessionDto : eventDateDto.getSessions()) {
                    LocalDateTime eventDateTime = LocalDateTime.of(eventDateDto.getDate(), LocalTime.parse(sessionDto.getTime()));
                    EventSchedule newSchedule = EventSchedule.builder()
                            .event(event)
                            .round(sessionDto.getRound())
                            .eventDate(eventDateTime)
                            .build();

                    createSeatMappings(seatDtoList, newSchedule, partitionMap);
                    event.addEventSchedule(newSchedule);
                }
            }

            // 5. 삭제된 스케줄 처리
            eventScheduleRepository.deleteAll(schedulesToDelete);
        } else {
            for (EventDateDto eventDateDto : schedulesToAdd) {
                for (SessionDto sessionDto : eventDateDto.getSessions()) {
                    LocalDateTime eventDateTime = LocalDateTime.of(eventDateDto.getDate(), LocalTime.parse(sessionDto.getTime()));
                    EventSchedule newSchedule = EventSchedule.builder()
                            .event(event)
                            .round(sessionDto.getRound())
                            .eventDate(eventDateTime)
                            .build();

                    // 기존 형식의 좌석 매핑 추가
                    if (!event.getStagePartitions().isEmpty()) {
                        event.getStagePartitions().forEach(stagePartition -> {
                            stagePartition.getSeatMappings().forEach(existingMapping -> {
                                StageSeat stageSeat = existingMapping.getStageSeat();

                                SeatMapping seatMapping = SeatMapping.builder()
                                        .eventSchedule(newSchedule)
                                        .stagePartition(stagePartition)
                                        .stageSeat(stageSeat)
                                        .build();

                                stagePartition.addSeatMapping(seatMapping);
                                newSchedule.addSeatMapping(seatMapping);
                            });
                        });
                    }

                    event.addEventSchedule(newSchedule);
                }
            }

            eventScheduleRepository.deleteAll(schedulesToDelete);
        }
    }


    // 이미지 업데이트
    private void updateEventImages(Event findEvent, MultipartFile poster, MultipartFile banner) {
        if (poster != null) updatePoster(findEvent, poster);
        if (banner != null) updateBanner(findEvent, banner);
    }

    private void updateBanner(Event event, MultipartFile banner) {
        if (banner == null || banner.isEmpty()) {
            return;
        }

        // 기존 배너 삭제
        String bannerOriginFileName = FileUtils.extractFileName(event.getEventImage().getBannerOriginUrl());
        String bannerMobileFileName = FileUtils.extractFileName(event.getEventImage().getBannerMobileUrl());
        String bannerPcFileName = FileUtils.extractFileName(event.getEventImage().getBannerPcUrl());

        awsS3Service.deleteBannerImage(bannerOriginFileName);
        awsS3Service.deleteResizedBannerImage(bannerMobileFileName, bannerPcFileName);

        String bannerOriginUrl = awsS3Service.uploadBannerOriginImage(banner);
        // 리사이즈된 배너 이미지 URL 생성
        String bannerPcUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_BANNER, FileUtils.extractFileName(bannerOriginUrl));
        String bannerMobileUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_MOBILE_BANNER, FileUtils.extractFileName(bannerOriginUrl));

        event.getEventImage().updateBanner(bannerOriginUrl, bannerPcUrl, bannerMobileUrl);
    }

    private void updatePoster(Event event, MultipartFile poster) {
        if (poster == null || poster.isEmpty()) {
            return;
        }

        // 기존 포스터 삭제
        String posterOriginFileName = FileUtils.extractFileName(event.getEventImage().getPosterOriginUrl());
        String posterMobileFileName = FileUtils.extractFileName(event.getEventImage().getPosterMobileUrl());
        String posterPcFileName = FileUtils.extractFileName(event.getEventImage().getPosterPcUrl());
        String posterPcMain1FileName = FileUtils.extractFileName(event.getEventImage().getPosterPcMain1Url());
        String posterPcMain2FileName = FileUtils.extractFileName(event.getEventImage().getPosterPcMain2Url());

        awsS3Service.deletePosterImage(posterOriginFileName);
        awsS3Service.deleteResizedPosterImage(posterMobileFileName, posterPcFileName, posterPcMain1FileName, posterPcMain2FileName);

        // 원본 포스터 업로드
        String newPosterOriginUrl = awsS3Service.uploadPosterOriginImage(poster);

        // 리사이즈된 포스터 이미지 URL 생성
        String posterMobileUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_MOBILE_POSTER, FileUtils.extractFileName(newPosterOriginUrl));
        String posterPcUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER, FileUtils.extractFileName(newPosterOriginUrl));
        String posterPcMain1Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN1, FileUtils.extractFileName(newPosterOriginUrl));
        String posterPcMain2Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN2, FileUtils.extractFileName(newPosterOriginUrl));

        event.getEventImage().updatePoster(newPosterOriginUrl, posterMobileUrl, posterPcUrl, posterPcMain1Url, posterPcMain2Url);
    }

    /**
     * 단일 이벤트 조회
     */
    public EventDetail getEventById(Long eventId) {
        Event findEvent = findEventByEventId(eventId);
        CompanyResponse companyResponse = adminServiceClient.getCompany(findEvent.getCompanyId());
        return EventDetail.toEventDetail(findEvent, companyResponse.getCompanyName());
    }

    @Caching(evict = {
            @CacheEvict(cacheNames = "events", key = "#eventId"),
            @CacheEvict(cacheNames = "searchEventScheduledOpen", allEntries = true)
    })
    @Transactional
    public void deleteEvent(Long eventId) {
        Event findEvent = findEventByEventId(eventId);
        EventImage eventImage = findEvent.getEventImage();

        // 이미지 삭제 처리
        deleteEventImages(eventImage);

        // 랭킹 삭제 처리
        deleteEventFromRankings(eventId);

        // 수동 캐시 삭제 처리
        deleteCache(eventId);

        // 이벤트 삭제
        eventRepository.delete(findEvent);
    }

    /**
     * Redis의 캐시 데이터에서 특정 이벤트를 제거합니다.
     *
     * @param eventId 삭제할 이벤트 ID.
     */
    private void deleteCache(Long eventId) {
        String detailCacheKey = RedisKeyHelper.getEventDetailCacheKey(eventId);
        redisTemplate.delete(detailCacheKey);
    }

    /**
     * Redis의 모든 랭킹 데이터에서 특정 이벤트를 제거합니다.
     *
     * @param eventId 삭제할 이벤트 ID.
     */
    private void deleteEventFromRankings(Long eventId) {
        // 장르별 랭킹에서 해당 이벤트 삭제
        Period[] periods = values();
        Genre[] genres = Genre.values();

        for (Period period : periods) {
            for (Genre genre : genres) {
                String rankingKey = RedisKeyHelper.getReservationKey(period, genre);
                rankingRedisTemplate.opsForZSet().remove(rankingKey, eventId.toString());
                log.info("Removed eventId {} from ranking: {}", eventId, rankingKey);
            }
        }

        // 조회수 랭킹에서 해당 이벤트 삭제
        String viewRankingKey = RedisKeyHelper.getViewRankingKey();
        redisTemplate.opsForZSet().remove(viewRankingKey, eventId);
    }

    // 이벤트 이미지 삭제 처리
    private void deleteEventImages(EventImage eventImage) {
        if (eventImage == null) return;

        // 포스터 이미지 파일명 추출
        String posterOrigin = FileUtils.extractFileName(eventImage.getPosterOriginUrl());
        String posterMobile = FileUtils.extractFileName(eventImage.getPosterMobileUrl());
        String posterPc = FileUtils.extractFileName(eventImage.getPosterPcUrl());
        String posterMain1 = FileUtils.extractFileName(eventImage.getPosterPcMain1Url());
        String posterMain2 = FileUtils.extractFileName(eventImage.getPosterPcMain2Url());

        // 배너 이미지 파일명 추출
        String bannerOrigin = FileUtils.extractFileName(eventImage.getBannerOriginUrl());
        String bannerMobile = FileUtils.extractFileName(eventImage.getBannerMobileUrl());
        String bannerPc = FileUtils.extractFileName(eventImage.getBannerPcUrl());

        // AWS S3 이미지 삭제
        deletePosterImages(posterOrigin, posterMobile, posterPc, posterMain1, posterMain2);
        deleteBannerImages(bannerOrigin, bannerMobile, bannerPc);
    }

    // 포스터 이미지 삭제
    private void deletePosterImages(String origin, String mobile, String pc, String main1, String main2) {
        awsS3Service.deletePosterImage(origin);
        awsS3Service.deleteResizedPosterImage(mobile, pc, main1, main2);
    }

    // 배너 이미지 삭제
    private void deleteBannerImages(String origin, String mobile, String pc) {
        awsS3Service.deleteBannerImage(origin);
        awsS3Service.deleteResizedBannerImage(mobile, pc);
    }

    @Transactional
    public String convertImageToUrl(MultipartFile image) {
        return awsS3Service.upload(image, CONTENT_BUCKET_NAME, ORIGIN_CONTENT_FOLDER);
    }


    private Event findEventByEventId(Long eventId) {
        return eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));
    }

    private EventStage findEventStageByStageId(Long stageId) {
        return eventStageRepository.findById(stageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
    }

    @Cacheable(
            cacheNames = "events", // 캐시 이름 설정
            key = "#eventScheduleId"    // 고정된 키 값 사용
    )
    public EventSeatSummary getEventByScheduleId(Long eventScheduleId) {
        EventSchedule eventSchedule = eventScheduleRepository.findById(eventScheduleId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_SESSION_NOT_FOUND));

        Event event = eventSchedule.getEvent();

        String posterMobileUrl = event.getEventImage().getPosterMobileUrl();
        Integer reservationLimit = event.getReservationLimit();
        String eventStageImg = event.getEventStage().getEventStageImg();
        List<SeatGradeInfo> seatGradeInfoList = event.getStagePartitions().stream()
                .map(stagePartition -> new SeatGradeInfo(stagePartition.getPartitionName(), stagePartition.getPartitionPrice()))
                .toList();

        return new EventSeatSummary(posterMobileUrl, reservationLimit, eventStageImg, seatGradeInfoList);
    }

    /**
     * 이벤트 상세 조회 및 조회수 증가
     */
    public EventDetailRes getEventDetail(HttpServletRequest request, HttpServletResponse response, Long eventId) {
        // 1. 캐시 키 생성
        String cacheKey = RedisKeyHelper.getEventDetailCacheKey(eventId);

        // 2. 캐시에서 데이터 확인
        EventDetailRes cachedEvent = (EventDetailRes) redisTemplate.opsForValue().get(cacheKey);

        // 3. 쿠키를 통해 중복 조회 확인
        if (!isDuplicateView(request, response, eventId)) {
            incrementViewCount(eventId); // 조회수 증가
        }

        // 4. 캐시가 존재하면 반환
        if (cachedEvent != null) {
            return cachedEvent;
        }

        // 5. 캐시가 없으면 DB 조회
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

        // 6. 변환 및 캐시에 저장
        EventDetailRes eventDetailRes = EventDetailRes.toEventDetailRes(event, "TEST");
        redisTemplate.opsForValue().set(cacheKey, eventDetailRes, Duration.ofHours(24)); // 24시간 TTL

        return eventDetailRes;
    }

    /**
     * 쿠키를 통해 중복 조회 방지
     */
    private boolean isDuplicateView(HttpServletRequest request, HttpServletResponse response, Long eventId) {
        Cookie[] cookies = request.getCookies();
        Cookie viewCountCookie = findCookie(cookies, "Event_View_Count");

        if (viewCountCookie != null) {
            // 쿠키에 해당 eventId가 포함되어 있는지 확인
            if (viewCountCookie.getValue().contains("[" + eventId + "]")) {
                return true; // 중복 조회
            }

            // 쿠키에 eventId 추가
            viewCountCookie.setValue(viewCountCookie.getValue() + "[" + eventId + "]");
            viewCountCookie.setPath("/");
            viewCountCookie.setHttpOnly(true); // 보안 강화
            viewCountCookie.setSecure(request.isSecure()); // HTTPS 요청만 Secure 설정
            response.addCookie(viewCountCookie);
        } else {
            // 쿠키가 없는 경우 새로 생성
            Cookie newCookie = new Cookie("Event_View_Count", "[" + eventId + "]");
            newCookie.setPath("/");
            newCookie.setHttpOnly(true); // 보안 강화
            newCookie.setSecure(request.isSecure()); // HTTPS 요청만 Secure 설정
            response.addCookie(newCookie);
        }

        return false; // 중복이 아님
    }

    /**
     * 조회수 증가
     */
    private void incrementViewCount(Long eventId) {
        String rankingKey = RedisKeyHelper.getViewRankingKey(); // 랭킹 키 생성
        rankingRedisTemplate.opsForZSet().incrementScore(rankingKey, String.valueOf(eventId), 1);
    }

    /**
     * 쿠키 찾기
     */
    private Cookie findCookie(Cookie[] cookies, String cookieName) {
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(cookieName)) {
                return cookie;
            }
        }
        return null;
    }

    /**
     * @param limit 조회할 랭크 개수 (default : 10)
     * @return limit개의 이벤트 순위 정보
     */
    public List<ViewRankResponse> getTopRankedEvents(int limit) {
        List<Object> rawEventIds = new ArrayList<>(Objects.requireNonNull(rankingRedisTemplate.opsForZSet()
                .reverseRange(RedisKeyHelper.getViewRankingKey(), 0, limit - 1)));

        List<Long> eventIds = rawEventIds.stream()
                .map(id -> Long.parseLong(id.toString())) // Object → String → Long 변환
                .toList();

        return eventIds.stream()
                .map(eventId -> {
                    String cacheKey = RedisKeyHelper.getEventDetailCacheKey(eventId);
                    EventDetailRes cachedEvent = (EventDetailRes) redisTemplate.opsForValue().get(cacheKey);

                    if (cachedEvent == null) {
                        Event event = eventRepository.findById(eventId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

                        EventDetailRes eventDetailRes = EventDetailRes.toEventDetailRes(event, "TEST");
                        redisTemplate.opsForValue().set(cacheKey, eventDetailRes, Duration.ofHours(24)); // 24시간 TTL
                        return ViewRankResponse.toViewRankResponse(eventId, eventDetailRes);
                    }

                    return ViewRankResponse.toViewRankResponse(eventId, cachedEvent);
                })
                .filter(Objects::nonNull) // null 제거
                .toList();
    }


    /**
     * 특정 장르와 기간에 대한 상위 N개의 예매율 순위를 조회합니다.
     *
     * @param genre  조회할 이벤트의 장르 (Genre Enum).
     * @param period 조회할 기간 (Period Enum).
     * @return 이벤트 세부 정보와 예매율 정보를 포함한 ReservationRateEventInfoResponse 목록.
     */
    public List<ReservationRateEventInfoResponse> getTopNReservationRateRank(Genre genre, Period period, int topN) {
        period = adjustPeriodByCutoffTime(period);

        List<Pair<Long, BigDecimal>> eventIdWithScores = getTopEventsFromRedis(genre, period, topN);
        if (eventIdWithScores.isEmpty()) {
            return Collections.emptyList();
        }

        BigDecimal totalSeatCount = getTotalSeatCountForPeriod(period);
        return fetchEventDetails(eventIdWithScores, totalSeatCount);
    }

    /**
     * 오전 10시 30분 이전에 요청된 경우, 기간을 전일 또는 전주로 변경합니다.
     *
     * @param period 입력된 기간 (Period Enum).
     * @return 조정된 기간 (Period Enum).
     */
    private Period adjustPeriodByCutoffTime(Period period) {
        LocalTime now = LocalTime.now();
        LocalTime cutoffTime = LocalTime.of(10, 30);

        if (now.isBefore(cutoffTime)) {
            if (period == DAILY) return PREVIOUS_DAILY;
            if (period == WEEKLY) return PREVIOUS_WEEKLY;
        }
        return period;
    }

    /**
     * Redis에서 특정 장르와 기간의 상위 N개의 이벤트 ID 및 점수를 조회합니다.
     *
     * @param genre  조회할 이벤트의 장르 (Genre Enum).
     * @param period 조회할 기간 (Period Enum).
     * @return 이벤트 ID와 점수를 포함한 Pair 목록.
     */
    private List<Pair<Long, BigDecimal>> getTopEventsFromRedis(Genre genre, Period period, int topN) {
        Set<ZSetOperations.TypedTuple<Object>> rankedEvents = rankingRedisTemplate.opsForZSet()
                .reverseRangeWithScores(RedisKeyHelper.getReservationKey(period, genre), 0, topN - 1);

        if (rankedEvents == null || rankedEvents.isEmpty()) {
            return Collections.emptyList();
        }

        return rankedEvents.stream()
                .map(tuple -> Pair.of(
                        Long.parseLong(tuple.getValue().toString()),  // 이벤트 ID
                        BigDecimal.valueOf(tuple.getScore())          // 점수
                ))
                .toList();
    }

    /**
     * 이벤트 세부 정보를 조회하고 예매율 정보를 생성합니다.
     *
     * @param eventIdWithScores 이벤트 ID와 점수를 포함한 Pair 목록.
     * @param totalSeatCount    기간 동안의 전체 좌석 수.
     * @return ReservationRateEventInfoResponse 목록.
     */
    private List<ReservationRateEventInfoResponse> fetchEventDetails(List<Pair<Long, BigDecimal>> eventIdWithScores, BigDecimal totalSeatCount) {
        return eventIdWithScores.stream()
                .map(eventWithScore -> {
                    Long eventId = eventWithScore.getKey();
                    BigDecimal score = eventWithScore.getValue();

                    EventDetailRes eventDetail = getEventDetailFromCacheOrDB(eventId);
                    if (eventDetail == null) return null;

                    return ReservationRateEventInfoResponse.toReservationRateEventInfoResponse(eventId, eventDetail, score, totalSeatCount);
                })
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Redis 캐시에서 이벤트 세부 정보를 조회하거나 DB에서 조회합니다.
     *
     * @param eventId 조회할 이벤트 ID.
     * @return 이벤트 세부 정보 (EventDetailRes).
     */
    private EventDetailRes getEventDetailFromCacheOrDB(Long eventId) {
        String cacheKey = RedisKeyHelper.getEventDetailCacheKey(eventId);
        EventDetailRes cachedEvent = (EventDetailRes) redisTemplate.opsForValue().get(cacheKey);

        if (cachedEvent == null) {
            Event event = eventRepository.findById(eventId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.EVENT_NOT_FOUND));

            EventDetailRes eventDetailRes = EventDetailRes.toEventDetailRes(event, "TEST");
            redisTemplate.opsForValue().set(cacheKey, eventDetailRes, Duration.ofHours(24));
            return eventDetailRes;
        }
        return cachedEvent;
    }

    /**
     * 특정 기간 동안의 전체 좌석 수를 조회합니다.
     *
     * @param period 조회할 기간 (Period Enum).
     * @return 해당 기간의 전체 좌석 수 (BigDecimal).
     */
    public BigDecimal getTotalSeatCountForPeriod(Period period) {
        String cacheKey = RedisKeyHelper.getTotalSeatCount(period);
        BigDecimal totalSeatCount = (BigDecimal) redisTemplate.opsForValue().get(cacheKey);

        if (totalSeatCount == null) {
            LocalDateTime[] range = getPeriodRange(period);
            totalSeatCount = stageSeatRepository.findTotalSeatsForPeriod(range[0], range[1]);
            redisTemplate.opsForValue().set(cacheKey, totalSeatCount, Duration.ofHours(1));
        }
        return totalSeatCount;
    }

    /**
     * 특정 기간에 대한 시작일과 종료일을 계산합니다.
     *
     * @param period 조회할 기간 (Period Enum).
     * @return 시작일과 종료일을 포함한 LocalDateTime 배열.
     */
    private LocalDateTime[] getPeriodRange(Period period) {
        LocalDate date = LocalDate.now();
        return switch (period) {
            case DAILY -> getStartAndNowOfDay(date);
            case PREVIOUS_DAILY -> getStartAndEndOfDay(date.minusDays(1));
            case WEEKLY -> getStartAndEndOfWeek(date);
            case PREVIOUS_WEEKLY -> getStartAndEndOfWeek(date.minusWeeks(1));
            case MONTHLY -> getStartAndEndOfMonth(date);
            default -> throw new BusinessException(ErrorCode.INPUT_VALUE_INVALID);
        };
    }

    private LocalDateTime[] getStartAndNowOfDay(LocalDate date) {
        return new LocalDateTime[]{date.atStartOfDay(), LocalDateTime.now()};
    }

    /**
     * 특정 날짜의 시작 시간과 종료 시간을 반환합니다.
     *
     * @param date 기준 날짜.
     * @return 시작일과 종료일을 포함한 LocalDateTime 배열.
     */
    private LocalDateTime[] getStartAndEndOfDay(LocalDate date) {
        return new LocalDateTime[]{date.atStartOfDay(), date.atTime(LocalTime.MAX)};
    }

    /**
     * 특정 날짜를 기준으로 주의 시작일(월요일)과 종료일(일요일)을 반환합니다.
     *
     * @param date 기준 날짜.
     * @return 주의 시작일과 종료일을 포함한 LocalDateTime 배열.
     */
    private LocalDateTime[] getStartAndEndOfWeek(LocalDate date) {
        LocalDate startOfWeek = date.with(DayOfWeek.MONDAY);
        LocalDate endOfWeek = date.with(DayOfWeek.SUNDAY);
        return new LocalDateTime[]{startOfWeek.atStartOfDay(), endOfWeek.atTime(LocalTime.MAX)};
    }

    /**
     * 특정 날짜를 기준으로 월의 시작일(1일)과 종료일(말일)을 반환합니다.
     *
     * @param date 기준 날짜.
     * @return 월의 시작일과 종료일을 포함한 LocalDateTime 배열.
     */
    private LocalDateTime[] getStartAndEndOfMonth(LocalDate date) {
        LocalDate startOfMonth = date.with(TemporalAdjusters.firstDayOfMonth());
        LocalDate endOfMonth = date.with(TemporalAdjusters.lastDayOfMonth());
        return new LocalDateTime[]{startOfMonth.atStartOfDay(), endOfMonth.atTime(LocalTime.MAX)};
    }


    public PagedResponse<EventSearchListRes> searchEvent(EventSearchCond eventSearchCond, Pageable pageable) {
        // 이벤트 검색 결과 가져오기
        List<EventSearchRes> eventSearchRes = eventRepository.searchEventByCond(eventSearchCond);

        if (eventSearchRes.isEmpty()) {
            return new PagedResponse<>(Collections.emptyList(), pageable.getPageNumber(), pageable.getPageSize(), 0, 0);
        }

        // adminId와 companyId를 수집
        Set<Long> adminIds = eventSearchRes.stream()
                .map(EventSearchRes::getAdminId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> companyIds = eventSearchRes.stream()
                .map(EventSearchRes::getCompanyId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // Admin 및 Company 정보 한 번에 조회
        Map<Long, String> adminNameMap = adminIds.isEmpty() ? Map.of() :
                executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getAdminsBatchCircuitBreaker",
                        () -> adminServiceClient.getAdminsByIds(adminIds)
                ).stream().collect(Collectors.toMap(AdminDto::getAdminId, AdminDto::getAdminName));

        Map<Long, String> companyNameMap = companyIds.isEmpty() ? Map.of() :
                executeWithCircuitBreaker(circuitBreakerRegistry,
                        "getCompaniesBatchCircuitBreaker",
                        () -> adminServiceClient.getCompaniesByIds(companyIds)
                ).stream().collect(Collectors.toMap(CompanyResponse::getCompanyId, CompanyResponse::getCompanyName));

        // 결과 변환
        List<EventSearchListRes> results = eventSearchRes.stream()
                .collect(Collectors.groupingBy(EventSearchRes::getEventId))
                .entrySet().stream()
                .map(entry -> {
                    Long eventId = entry.getKey();
                    List<EventSearchRes> groupedEvents = entry.getValue();
                    EventSearchRes firstEvent = groupedEvents.get(0);

                    return EventSearchListRes.builder()
                            .eventId(eventId)
                            .eventTitle(firstEvent.getEventTitle())
                            .stageName(firstEvent.getStageName())
                            .adminId(firstEvent.getAdminId())
                            .companyName(companyNameMap.get(firstEvent.getCompanyId()))
                            .adminName(adminNameMap.get(firstEvent.getAdminId()))
                            .eventDates(groupedEvents.stream()
                                    .map(EventSearchRes::getEventDate)
                                    .sorted() // 날짜 정렬
                                    .collect(Collectors.toList()))
                            .build();
                })
                .sorted(Comparator.comparing(EventSearchListRes::getEventId)) // 정렬 기준
                .collect(Collectors.toList());

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<EventSearchListRes> pagedResults = results.subList(start, end);

        // PagedResponse로 변환하여 반환
        return new PagedResponse<>(
                pagedResults,
                pageable.getPageNumber(),
                pageable.getPageSize(),
                results.size(),
                (int) Math.ceil((double) results.size() / pageable.getPageSize())
        );
    }

    public List<TicketInfoDto> getMyTicketInfo(TicketInfoCreateDtoList ticketInfoCreateDtoList) {

        List<Long> ticketIds = ticketInfoCreateDtoList.getTicketInfoCreateDtoList().stream().map(TicketInfoCreateDto::getTicketId).toList();
        // 1. 티켓 정보 조회
        List<TicketEventResponse> ticketEventResponseList = eventRepository.getMyTicketInfo(ticketIds);

        // 2. companyId 수집
        Set<Long> companyIds = ticketEventResponseList.stream()
                .map(TicketEventResponse::getCompanyId)
                .collect(Collectors.toSet());

        // 3. 회사 정보 조회
        List<CompanyResponse> companyResponses = executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getCompaniesBatchCircuitBreaker",
                () -> adminServiceClient.getCompaniesByIds(companyIds)
        );

        Map<Long, String> companyNameMap = companyResponses.stream()
                .collect(Collectors.toMap(CompanyResponse::getCompanyId, CompanyResponse::getCompanyName));


        Map<Long, LocalDateTime> ticketCreateMap = ticketInfoCreateDtoList.getTicketInfoCreateDtoList().stream()
                .collect(Collectors.toMap(TicketInfoCreateDto::getTicketId, TicketInfoCreateDto::getCreatedAt));

        // 5. TicketId -> OrderId 매핑
        Map<Long, Long> ticketOrderMap = ticketInfoCreateDtoList.getTicketInfoCreateDtoList()
                .stream()
                .collect(Collectors.toMap(TicketInfoCreateDto::getTicketId, TicketInfoCreateDto::getOrderId));

        // 4. MapStruct 매퍼를 사용하여 변환
        return ticketEventResponseList.stream()
                .map(ticket -> ticketMapper.toTicketInfoDto(ticket, companyNameMap, ticketCreateMap, ticketOrderMap))
                .toList();
    }

    public LocalDateTime getEventDateTime(Long eventScheduleId) {
        return eventScheduleRepository.findEventDateByEventScheduleId(eventScheduleId);
    }

    public List<Long> getTodayOpenEvents() {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfToday = now.atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.of(now, LocalTime.MAX);
        return eventRepository.findOpenEvents(startOfToday, endOfToday);
    }

    public List<Long> getYesterdayOpenEvents() {
        LocalDate now = LocalDate.now().minusDays(1);
        LocalDateTime startOfToday = now.atStartOfDay();
        LocalDateTime endOfToday = LocalDateTime.of(now, LocalTime.MAX);
        return eventRepository.findOpenEvents(startOfToday, endOfToday);
    }

    /**
     * 오픈 예정 티켓 조회
     *
     * @param cond     조건
     * @param pageable 페이징
     * @return 오픈 예정 티켓
     */
    @Cacheable(
            value = "searchEventScheduledOpen",
            key = "#cond.hashCode() + '-' + #pageable.pageNumber + '-' + #pageable.pageSize + '-' + #pageable.sort.toString()"
    )
    public PageDTO<EventScheduledOpenResponse> searchOpenEvent(EventScheduledOpenSearchCond cond, Pageable pageable) {
        Page<EventScheduledOpenResponse> result = eventRepository.searchEventScheduledOpen(cond, pageable);

        return PageDTO.toPageDTO(result);
    }


    public List<Long> getCompanyId(Long ticketId) {
        List<Long> res = new ArrayList<>();
        SeatMapping seatMapping = seatMappingRepository.findByTicketId(ticketId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));
        Event event = seatMapping.getEventSchedule().getEvent();
        res.add(event.getCompanyId());
        res.add(event.getEventId());
        return res;
    }

    public List<EventTitleDto> getTitleIds(String title) {
        List<EventTitleDto> res = eventRepository.findEventIds(title);
        return res;
    }

    public List<String> getTitle() {
        return eventRepository.findEventTitle();
    }


    public List<SimpleEvent> getOpenRecent(String genre){
        return eventRepository.openSearchTop6Genre(genre);
    }

    public List<SimpleEvent> getGenreRank(String genre){
        return eventRepository.getGenreRank(genre);
    }
    public List<String> allArea(){
        return eventStageRepository.findAllSido();
    }

    public SimplePageRes getGenreList(Genre genre, String area, Period period, Pageable pageable) {
        period = adjustPeriodByCutoffTime(period);

        List<Pair<Long, BigDecimal>> eventIdWithScores = getTopEventsFromRedis(genre, period, 100);

        List<Long> eventIds = eventIdWithScores.stream()
                .map(Pair::getKey) // Pair의 첫 번째 값(Long)을 추출
                .toList();

        List<Event> rankEvents = eventRepository.findAllByAreaAndIds(area, eventIds);
        boolean last = true;
        // 충분이 크냐~
        System.out.println(rankEvents.size() + "  :  " + genre +" "  +period +" "+ pageable.toString());
        if ((pageable.getPageNumber() + 1) * pageable.getPageSize() > rankEvents.size()){
            int remainingSize = (pageable.getPageNumber() + 1) * pageable.getPageSize() - rankEvents.size();
            Page<Event> pageEvents = eventRepository.findExcludingIds(eventIds, area, PageRequest.of(pageable.getPageNumber(), remainingSize + 1));
            List<Event> eventList = pageEvents.getContent();
            if(eventList.size() == remainingSize + 1){
                eventList.remove(eventList.size() - 1);
                last = false;
            }
            rankEvents.addAll(eventList);
        }else{
            // 여기서 last 구하기
            last = false;
            int start = pageable.getPageNumber() * pageable.getPageSize();
            int end = Math.min(start + pageable.getPageSize(), rankEvents.size());
            rankEvents = rankEvents.subList(start, end);
        }
        List<SimpleEvent> content = eventMapper.toSimpleEventList(rankEvents);

        return SimplePageRes.builder()
                .content(content)
                .last(last)
                .page(pageable.getPageNumber())
                .size(pageable.getPageSize())
                .build();
    }
}
