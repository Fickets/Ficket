package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.request.SeatDto;
import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
import com.example.ficketevent.domain.event.dto.response.SimpleEvent;
import com.example.ficketevent.domain.event.entity.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface EventMapper {

    @Mappings({
            @Mapping(target = "eventSchedules", ignore = true),
            @Mapping(target = "stagePartitions", ignore = true),
            @Mapping(target = "eventId", ignore = true),
            @Mapping(target = "eventImage", ignore = true),
    })
    Event eventDtoToEvent(EventCreateReq eventCreateReq, Long companyId, Long adminId, EventStage eventStage);

    @Mappings({
            @Mapping(source = "grade", target = "partitionName"),
            @Mapping(source = "price", target = "partitionPrice"),
            @Mapping(target = "partitionId", ignore = true),
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "seatMappings", ignore = true),
    })
    StagePartition toStagePartition(SeatDto seatDto);

    // 전체 List<SeatDto>를 변환하기 위한 메서드
    default List<StagePartition> toStagePartitions(List<SeatDto> seatDtos) {
        return seatDtos.stream()
                .map(this::toStagePartition) // 개별 매핑 메서드를 호출하여 변환
                .collect(Collectors.toList());
    }


    @Mappings({
            @Mapping(target = "createdAt", ignore = true),
            @Mapping(target = "lastModifiedAt", ignore = true),
            @Mapping(target = "deletedAt", ignore = true),
            @Mapping(target = "event", ignore = true),
            @Mapping(target = "eventImgId", ignore = true),
    })
    EventImage toEventImage(String posterOriginUrl, String bannerOriginUrl, String posterMobileUrl, String posterPcUrl, String posterPcMain1Url, String posterPcMain2Url, String bannerPcUrl, String bannerMobileUrl);


    default List<SimpleEvent> toSimpleEventList(List<Event> eventList) {
        return eventList.stream().map(event -> {
            List<EventSchedule> schedules = event.getEventSchedules();
            String date = event.getTicketingTime().toString().split("T")[0];
            if (!schedules.isEmpty()) {
                EventSchedule first = schedules.get(0);
                EventSchedule second = schedules.get(schedules.size() - 1);
                String firstDate = first.getEventDate().toString().split("T")[0];
                String secondDate = second.getEventDate().toString().split("T")[0];
                date = firstDate + "~" + secondDate;
            }


            return SimpleEvent.builder()
                    .eventId(event.getEventId())
                    .title(event.getTitle())
                    .date(date)
                    .pcImg(event.getEventImage().getPosterPcMain1Url())
                    .eventStage(event.getEventStage().getStageName())
                    .mobileImg(event.getEventImage().getPosterPcMain2Url())
                    .build();
        }).toList();
    }

}