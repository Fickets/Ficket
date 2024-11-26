package com.example.ficketevent.domain.event.dto.response;

import com.example.ficketevent.domain.event.dto.request.EventDateDto;
import com.example.ficketevent.domain.event.dto.request.SeatDto;
import com.example.ficketevent.domain.event.dto.request.SessionDto;
import com.example.ficketevent.domain.event.entity.Event;
import com.example.ficketevent.domain.event.entity.EventSchedule;
import com.example.ficketevent.domain.event.entity.StageSeat;
import com.example.ficketevent.domain.event.enums.Age;
import com.example.ficketevent.domain.event.enums.Genre;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EventDetail {
    private Long adminId; // 관리자 ID
    private Long companyId; // 회사 ID
    private Long stageId; // 공연장 ID
    private List<Genre> genre; // 장르 목록
    private Age age; // 관람 연령
    private String content; // 공연 상세 내용
    private String title; // 제목
    private String subTitle; // 부제목
    private Integer runningTime; // 상영 시간
    private LocalDateTime ticketingTime; // 티켓팅 시작 시간
    private Integer reservationLimit; // 1인당 티켓 매수 제한
    private List<EventDateDto> eventSchedules; // 행사 날짜 정보
    private List<SeatDto> stageSeats; // 좌석 정보

    public static EventDetail toEventDetail(Event event) {
        // 날짜별 그룹화 및 변환
        List<EventDateDto> eventSchedules = event.getEventSchedules().stream()
                .collect(Collectors.groupingBy(schedule -> schedule.getEventDate().toLocalDate()))
                .entrySet().stream()
                .map(entry -> EventDateDto.builder()
                        .date(entry.getKey()) // LocalDate
                        .sessions(entry.getValue().stream()
                                .map(schedule -> SessionDto.builder()
                                        .round(schedule.getRound())
                                        .time(schedule.getEventDate().toLocalTime().toString()) // HH:mm 형식
                                        .build())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        // 좌석 정보 변환
        List<SeatDto> stageSeats = event.getStagePartitions().stream()
                .map(partition -> SeatDto.builder()
                        .grade(partition.getPartitionName())
                        .price(partition.getPartitionPrice())
                        .seats(partition.getSeatMappings().stream()
                                .map(seatMapping -> seatMapping.getStageSeat().getSeatId())
                                .collect(Collectors.toList()))
                        .build())
                .collect(Collectors.toList());

        // EventDetail 빌드
        return EventDetail.builder()
                .adminId(event.getAdminId())
                .companyId(event.getCompanyId())
                .stageId(event.getEventStage().getStageId())
                .genre(event.getGenre())
                .age(event.getAge())
                .content(event.getContent())
                .title(event.getTitle())
                .subTitle(event.getSubTitle())
                .runningTime(event.getRunningTime())
                .ticketingTime(event.getTicketingTime())
                .reservationLimit(event.getReservationLimit())
                .eventSchedules(eventSchedules)
                .stageSeats(stageSeats)
                .build();
    }
}
