package com.example.ficketevent.domain.event.service;

import com.example.ficketevent.domain.event.client.AdminServiceClient;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
import com.example.ficketevent.domain.event.entity.*;
import com.example.ficketevent.domain.event.mapper.EventMapper;
import com.example.ficketevent.domain.event.repository.EventRepository;
import com.example.ficketevent.domain.event.repository.EventStageRepository;
import com.example.ficketevent.domain.event.repository.StageSeatRepository;
import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.exception.BusinessException;
import com.example.ficketevent.global.utils.AwsS3Service;
import com.example.ficketevent.global.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;

import static com.example.ficketevent.global.config.awsS3.AwsConstants.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventService {

    private final AdminServiceClient adminServiceClient;
    private final EventMapper eventMapper;
    private final EventRepository eventRepository;
    private final EventStageRepository eventStageRepository;
    private final StageSeatRepository stageSeatRepository;
    private final AwsS3Service awsS3Service;

    /**
     * 이벤트 생성 메서드
     *
     * @param req    이벤트 생성 요청 DTO
     * @param poster 포스터 이미지 파일
     * @param banner 배너 이미지 파일
     * @throws BusinessException 잘못된 데이터가 있을 경우 예외 발생
     */
    @Transactional
    public void createEvent(EventCreateReq req, MultipartFile poster, MultipartFile banner) {
        try {

            // 추후 Feign Client를 통해 Admin 정보를 가져올 수 있도록 주석 처리
//            AdminDto adminDto = adminServiceClient.getAdmin(req.getAdminId());
            // 회사 정보 조회
            CompanyResponse companyResponse = adminServiceClient.getCompany(req.getCompanyId());

            // 스테이지 정보 조회
            EventStage eventStage = findEventStageByStageId(req.getStageId());

            // Event 생성
            Event newEvent = createNewEvent(req, companyResponse, eventStage);

            // 이벤트 일정 생성
            List<EventSchedule> eventSchedules = createEventSchedules(req, newEvent);
            eventSchedules.forEach(newEvent::addEventSchedule);

            // 스테이지 파티션 및 좌석 매핑 생성
            List<StagePartition> stagePartitions = createStagePartitions(req, newEvent, eventStage);
            stagePartitions.forEach(newEvent::addStagePartition);

            // S3에 포스터 및 배너 업로드 후, EventImage 생성
            EventImage eventImage = createEventImage(poster, banner);
            newEvent.addEventImage(eventImage);

            // 이벤트 저장
            eventRepository.save(newEvent);

        } catch (Exception e) {
            log.error("Event creation failed: {}", e.getMessage());
            throw e; // 트랜잭션 롤백을 위해 예외 다시 던짐
        }
    }

    @Transactional
    public String convertImageToUrl(MultipartFile image) {
        return awsS3Service.upload(image, CONTENT_BUCKET_NAME, ORIGIN_CONTENT_FOLDER);
    }

    /**
     * 새로운 이벤트 객체를 생성합니다.
     */
    private Event createNewEvent(EventCreateReq req, CompanyResponse companyResponse, EventStage eventStage) {
        Event event = eventMapper.eventDtoToEvent(req, companyResponse.getCompanyId(), eventStage);
        event.addEventStage(eventStage);
        return event;
    }

    /**
     * 이벤트 일정을 생성합니다.
     */
    private List<EventSchedule> createEventSchedules(EventCreateReq req, Event event) {
        return req.getEventDate().stream()
                .flatMap(eventDate -> eventDate.getSessions().stream()
                        .map(session -> EventSchedule.builder()
                                .event(event)
                                .round(session.getRound()) // 회차 정보 설정
                                .eventDate(LocalDateTime.of(eventDate.getDate(), LocalTime.parse(session.getTime()))) // 날짜 및 시간 설정
                                .build()))
                .collect(Collectors.toList());
    }

    /**
     * 스테이지 파티션 및 좌석 매핑을 생성합니다.
     */
    private List<StagePartition> createStagePartitions(EventCreateReq req, Event newEvent, EventStage eventStage) {
        return req.getSeats().stream()
                .map(seatDto -> {
                    StagePartition stagePartition = eventMapper.toStagePartition(seatDto); // 스테이지 파티션 생성
                    stagePartition.setEvent(newEvent);

                    // SeatMapping 생성
                    List<SeatMapping> seatMappings = seatDto.getSeats().stream()
                            .map(seatId -> createSeatMapping(seatId, stagePartition, eventStage))
                            .collect(Collectors.toList());

                    stagePartition.setSeatMappings(seatMappings); // 파티션에 SeatMapping 추가
                    return stagePartition;
                })
                .toList();
    }

    /**
     * 좌석 매핑을 생성합니다.
     */
    private SeatMapping createSeatMapping(Long seatId, StagePartition stagePartition, EventStage eventStage) {
        StageSeat stageSeat = stageSeatRepository.findById(seatId)
                .orElseThrow(() -> new BusinessException(ErrorCode.SEAT_NOT_FOUND));

        return SeatMapping.builder()
                .stageSeat(stageSeat)
                .stagePartition(stagePartition)
                .ticketId(null) // 초기값은 null
                .build();
    }

    /**
     * 이벤트 이미지를 생성합니다.
     */
    private EventImage createEventImage(MultipartFile poster, MultipartFile banner) {
        // 원본 포스터와 배너 업로드
        String posterOriginUrl = awsS3Service.uploadPosterOriginImage(poster);
        String bannerOriginUrl = awsS3Service.uploadBannerOriginImage(banner);

        // 리사이즈된 포스터 이미지 URL 생성
        String posterMobileUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_MOBILE_POSTER, FileUtils.extractFileName(posterOriginUrl));
        String posterPcUrl = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER, FileUtils.extractFileName(posterOriginUrl));
        String posterPcMain1Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN1, FileUtils.extractFileName(posterOriginUrl));
        String posterPcMain2Url = awsS3Service.getResizedImageUrl(RESIZED_BUCKET_NAME, RESIZED_PC_POSTER_MAIN2, FileUtils.extractFileName(posterOriginUrl));

        // 리사이즈된 배너 이미지 URL 생성
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
     * 스테이지 ID로 스테이지 정보를 조회합니다.
     */
    private EventStage findEventStageByStageId(Long stageId) {
        return eventStageRepository.findById(stageId)
                .orElseThrow(() -> new BusinessException(ErrorCode.STAGE_NOT_FOUND));
    }
}
