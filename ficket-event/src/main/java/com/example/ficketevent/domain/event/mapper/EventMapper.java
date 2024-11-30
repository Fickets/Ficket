package com.example.ficketevent.domain.event.mapper;

import com.example.ficketevent.domain.event.dto.request.SeatDto;
import com.example.ficketevent.domain.event.dto.request.EventCreateReq;
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
            @Mapping(target = "stagePartitions", ignore = true)
    })
    Event eventDtoToEvent(EventCreateReq eventCreateReq, Long companyId, Long adminId, EventStage eventStage);

    @Mappings({
            @Mapping(source = "grade", target = "partitionName"),
            @Mapping(source = "price", target = "partitionPrice")
    })
    StagePartition toStagePartition(SeatDto seatDto);

    // 전체 List<SeatDto>를 변환하기 위한 메서드
    default List<StagePartition> toStagePartitions(List<SeatDto> seatDtos) {
        return seatDtos.stream()
                .map(this::toStagePartition) // 개별 매핑 메서드를 호출하여 변환
                .collect(Collectors.toList());
    }

    EventImage toEventImage(String posterOriginUrl, String bannerOriginUrl, String posterMobileUrl, String posterPcUrl, String posterPcMain1Url, String posterPcMain2Url, String bannerPcUrl, String bannerMobileUrl);

}