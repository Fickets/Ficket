package com.example.ficketevent.domain.event.dto.request;

import com.example.ficketevent.domain.event.enums.Age;
import com.example.ficketevent.domain.event.enums.Genre;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class EventCreateReq {
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
    private List<EventDateDto> eventDate; // 행사 날짜 정보
    private List<SeatDto> seats; // 좌석 정보


}
