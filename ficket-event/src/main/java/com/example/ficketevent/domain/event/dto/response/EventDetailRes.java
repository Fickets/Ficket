package com.example.ficketevent.domain.event.dto.response;


import com.example.ficketevent.domain.event.dto.common.EventScheduleDto;
import com.example.ficketevent.domain.event.dto.common.PartitionDto;
import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.enums.Age;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Data
@Builder
public class EventDetailRes {

    private Long adminId; // 관리자 ID

    private Long companyId; // 회사 ID
    private String companyName; // 회사 이름

    private Long stageId; // 공연장 ID
    private String stageName; // 공연장 이름
    private String sido;
    private String sigungu;
    private String street;
    private String eventStageImg; // 공연장 좌석 배치도

    private List<String> genre; // 장르 목록
    private Age age; // 관람 연령
    private String content; // 공연 상세 내용
    private String title; // 제목
    private String subTitle; // 부제목
    private LocalDateTime ticketingTime; // 티켓팅 시작 시간
    private Integer runningTime; // 상영 시간
    private Integer reservationLimit; // 1인당 티켓 매수 제한

    private String posterMobileUrl; // 모바일 포스터 이미지 URL
    private String posterPcUrl; // PC 포스터 이미지 URL 작은거
    private String posterPcMainUrl; // PC 포스터 이미지 큰거
    private String bannerOriginUrl;
    private List<Map<String, String>> partitionPrice;
    private Map<String, HashMap<Integer, EventScheduleDto>> scheduleMap;

    public static EventDetailRes toEventDetailRes(Event event, String compName){
        Map<String, HashMap<Integer, EventScheduleDto>> scheduleMap = new TreeMap<>();
        List<Map<String, String>> partitionList = new ArrayList<>();
        event.getStagePartitions().forEach(partition -> {
            Map<String, String> partitionTmp = new HashMap<>();
            partitionTmp.put("partitionName", partition.getPartitionName());
            partitionTmp.put("partitionPrice", partition.getPartitionPrice().toString());
            partitionList.add(partitionTmp);
        });


        event.getEventSchedules().forEach(eventSchedule -> {
            String key = eventSchedule.getEventDate().toString().split("T")[0];
            EventScheduleDto value = EventScheduleDto.builder()
                    .eventScheduleId(eventSchedule.getEventScheduleId())
                    .round(eventSchedule.getRound())
                    .eventDate(eventSchedule.getEventDate())
                    .build();
            scheduleMap.computeIfAbsent(key, k -> new HashMap<>()).put(value.getRound(), value);
        });

        event.getStagePartitions().forEach(stagePartition ->  {
            String partitionName = stagePartition.getPartitionName();
            stagePartition.getSeatMappings().forEach(seatMapping -> {
                String date = seatMapping.getEventSchedule()
                        .getEventDate().toString().split("T")[0];
                Integer round = seatMapping.getEventSchedule().getRound();

                if (seatMapping.getTicketId() == null) {


                    HashMap<Integer, EventScheduleDto> hashEventScheduleDto = scheduleMap.get(date);

                    EventScheduleDto eventScheduleDto = hashEventScheduleDto.get(round);

                    PartitionDto tmp3 = eventScheduleDto.getPartition().computeIfAbsent(partitionName, k -> PartitionDto.builder()
                            .partitionName(partitionName)
                            .build());
                    tmp3.setRemainingSeats(tmp3.getRemainingSeats() + 1);

                    eventScheduleDto.getPartition().put(partitionName, tmp3);
                    hashEventScheduleDto.put(round, eventScheduleDto);
                    scheduleMap.put(date, hashEventScheduleDto);

                }
            });
        });

        return EventDetailRes.builder()
                .adminId(event.getAdminId())
                .companyId(event.getCompanyId())
                .companyName(compName)
                .stageId(event.getEventStage().getStageId())
                .stageName(event.getEventStage().getStageName())
                .sido(event.getEventStage().getSido())
                .sigungu(event.getEventStage().getSigungu())
                .street(event.getEventStage().getStreet())
                .eventStageImg(event.getEventStage().getEventStageImg())
                .genre(event.getGenre().stream()
                        .map(Enum::name)
                        .collect(Collectors.toList()))
                .age(event.getAge())
                .content(event.getContent())
                .title(event.getTitle())
                .subTitle(event.getSubTitle())
                .ticketingTime(event.getTicketingTime())
                .runningTime(event.getRunningTime())
                .reservationLimit(event.getReservationLimit())
                .posterPcUrl(event.getEventImage().getPosterPcMain2Url())
                .posterPcMainUrl(event.getEventImage().getPosterPcUrl())
                .posterMobileUrl(event.getEventImage().getPosterMobileUrl())
                .bannerOriginUrl(event.getEventImage().getBannerOriginUrl())
                .partitionPrice(partitionList)
                .scheduleMap(scheduleMap)
                .build();
    }
}
