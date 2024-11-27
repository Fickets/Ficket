package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.AdminServiceClient;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import com.example.ficketevent.domain.event.dto.request.*;
import com.example.ficketevent.domain.event.dto.response.EventDetail;
import com.example.ficketevent.domain.event.entity.*;
import com.example.ficketevent.domain.event.mapper.EventMapper;
import com.example.ficketevent.domain.event.repository.*;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class EventService {

    private final AdminServiceClient adminServiceClient;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final EventStageRepository eventStageRepository;
    private final EventScheduleRepository eventScheduleRepository;
    private final StagePartitionRepository stagePartitionRepository;
    private final StageSeatRepository stageSeatRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 이벤트 생성 메서드
     */
    @Transactional
    public void createEvent(EventCreateReq req, MultipartFile poster, MultipartFile banner) {
        // 1. 회사 및 공연장 정보 조회
        CompanyResponse companyResponse = adminServiceClient.getCompany(req.getCompanyId());
        EventStage eventStage = findEventStageByStageId(req.getStageId());

        // 2. 이벤트 생성
        Event newEvent = createNewEvent(req, companyResponse, eventStage);

        // 3. 파티션 생성
        Map<String, StagePartition> partitionMap = createPartitions(req, newEvent);

        // 4. 스케줄 및 좌석 매핑 생성
        createSchedulesAndMappings(req, newEvent, partitionMap);

        // 5. 이미지 생성 및 추가
        EventImage eventImage = createEventImage(poster, banner);
        newEvent.addEventImage(eventImage);

        // 6. 공연장과 이벤트 연결
        eventStage.getEvents().add(newEvent);
    }


    /**
     * 새로운 이벤트 생성
     */
    private Event createNewEvent(EventCreateReq req, CompanyResponse companyResponse, EventStage eventStage) {
        Event event = eventMapper.eventDtoToEvent(req, companyResponse.getCompanyId(), eventStage);
        event.addEventStage(eventStage);
        return event;
    }

    // 파티션 생성
    private Map<String, StagePartition> createPartitions(EventCreateReq req, Event newEvent) {
        Map<String, StagePartition> partitionMap = req.getSeats().stream()
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
    private void createSchedulesAndMappings(EventCreateReq req, Event newEvent, Map<String, StagePartition> partitionMap) {
        req.getEventDate().forEach(eventDateDto -> {
            eventDateDto.getSessions().forEach(sessionDto -> {
                EventSchedule eventSchedule = EventSchedule.builder()
                        .event(newEvent)
                        .round(sessionDto.getRound())
                        .eventDate(LocalDateTime.of(eventDateDto.getDate(), LocalTime.parse(sessionDto.getTime())))
                        .build();

                createSeatMappings(req, eventSchedule, partitionMap);
                newEvent.addEventSchedule(eventSchedule);
            });
        });
    }

    // 좌석 매핑 생성
    private void createSeatMappings(EventCreateReq req, EventSchedule eventSchedule, Map<String, StagePartition> partitionMap) {
        req.getSeats().forEach(seatDto -> {
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
    @CacheEvict(cacheNames = "events", key = "#eventId")
    @Transactional
    public void updateEvent(Long eventId, EventUpdateReq req, MultipartFile poster, MultipartFile banner) {
        Event findEvent = findEventByEventId(eventId);

        // 1. 회사 정보 업데이트
        updateCompanyInfo(req, findEvent);

        // 2. 스테이지 정보 업데이트
        updateStageInfo(req, findEvent);

        // 3. 이벤트 정보 업데이트
        findEvent.updatedEvent(req);

        // 4. 스케줄 및 좌석 매핑 업데이트
        if (req.getEventDate() != null) {
            updateEventSchedulesAndSeatMappings(req, findEvent);
        }

        // 5. 좌석 파티션 업데이트
        if (req.getSeats() != null) {
            updateStagePartitions(req, findEvent);
        }

        // 6. 이미지 업데이트
        updateEventImages(req, findEvent, poster, banner);
    }

    // 회사 정보 업데이트
    private void updateCompanyInfo(EventUpdateReq req, Event findEvent) {
        if (req.getCompanyId() != null) {
            CompanyResponse companyResponse = adminServiceClient.getCompany(req.getCompanyId());
            findEvent.setCompanyId(companyResponse.getCompanyId());
        }
    }

    // 스테이지 정보 업데이트
    private void updateStageInfo(EventUpdateReq req, Event findEvent) {
        if (req.getStageId() != null) {
            EventStage eventStage = findEventStageByStageId(req.getStageId());
            findEvent.setEventStage(eventStage);
        }
    }

    // 스케줄 및 좌석 매핑 업데이트
    private void updateEventSchedulesAndSeatMappings(EventUpdateReq req, Event findEvent) {
        // 기존 스케줄 삭제
        eventScheduleRepository.deleteByEvent(findEvent);

        // 새로운 스케줄 및 좌석 매핑 생성
        List<EventSchedule> updatedSchedules = req.getEventDate().stream()
                .flatMap(eventDateDto -> eventDateDto.getSessions().stream().map(sessionDto -> {
                    EventSchedule eventSchedule = EventSchedule.builder()
                            .event(findEvent)
                            .round(sessionDto.getRound())
                            .eventDate(LocalDateTime.of(eventDateDto.getDate(), LocalTime.parse(sessionDto.getTime())))
                            .build();

                    // 좌석 매핑 생성
                    createSeatMappings(req, eventSchedule);
                    return eventSchedule;
                }))
                .toList();

        updatedSchedules.forEach(findEvent::addEventSchedule); // 스케줄 추가
    }

    // 좌석 매핑 생성
    private void createSeatMappings(EventUpdateReq req, EventSchedule eventSchedule) {
        Map<String, StagePartition> partitionMap = getPartitionsByEvent(eventSchedule.getEvent()); // 파티션 조회

        req.getSeats().forEach(seatDto -> {
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

    // 좌석 파티션 업데이트
    private void updateStagePartitions(EventUpdateReq req, Event findEvent) {
        // 기존 파티션 삭제
        stagePartitionRepository.deleteByEvent(findEvent);

        // 새로운 파티션 생성
        List<StagePartition> updatedPartitions = req.getSeats().stream()
                .map(seatDto -> {
                    StagePartition stagePartition = StagePartition.builder()
                            .event(findEvent)
                            .partitionName(seatDto.getGrade())
                            .partitionPrice(seatDto.getPrice())
                            .build();

                    seatDto.getSeats().forEach(seatId -> {
                        StageSeat stageSeat = stageSeatRepository.findById(seatId)
                                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

                        SeatMapping seatMapping = SeatMapping.builder()
                                .stagePartition(stagePartition)
                                .stageSeat(stageSeat)
                                .build();

                        stagePartition.addSeatMapping(seatMapping);
                    });

                    return stagePartition;
                })
                .toList();

        updatedPartitions.forEach(findEvent::addStagePartition); // 파티션 추가
    }

    // 좌석 파티션 조회
    private Map<String, StagePartition> getPartitionsByEvent(Event event) {
        return event.getStagePartitions().stream()
                .collect(Collectors.toMap(StagePartition::getPartitionName, partition -> partition));
    }

    // 이미지 업데이트
    private void updateEventImages(EventUpdateReq req, Event findEvent, MultipartFile poster, MultipartFile banner) {
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

    @Transactional
    public void deleteEvent(Long eventId) {
        Event findEvent = findEventByEventId(eventId);
        EventImage eventImage = findEvent.getEventImage();

        // 이미지 삭제 처리
        deleteEventImages(eventImage);

        // 이벤트 삭제
        eventRepository.delete(findEvent);
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

}
