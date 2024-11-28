package com.example.ficketadmin.domain.event.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class EventController {

    //TODO   기능                 메소드/담당     path:/api/v1/admins
    
    //TODO  성별예매율조회           /GET/OHS     /{eventId}/gender-ratio
    //TODO  연령별예매율조회         /GET/OHS      /{eventId}/age-ratio
    //TODO  날짜별예매율조회         /GET/OHS      /{eventId}/daily-revenue
    //TODO  날짜별수익조회           /GET/OHS      /{eventId}/day-count
    //TODO  요일별예매수조회         /GET/OHS       /{eventId}/day-count
    //TODO  공연정보등록            /POST/OHS
    //TODO  공연정보수정            /PATCH/OHS
    //TODO  공연삭제               /DELETE/OHS
    //TODO  공연리스트조회          /GET/OHS
    //TODO  공연장리스트조회        /GET/OHS        /stages
    //TODO  임시URL발급           /POST/OHS        /tmp-url
    //TODO  임시URL검증           /GET/CYS         /validation/{uuid}



}
