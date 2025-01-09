package com.example.ficketevent.global.result.error;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {
    // Global
    INTERNAL_SERVER_ERROR(500, "내부 서버 오류입니다."),
    METHOD_NOT_ALLOWED(405, "허용되지 않은 HTTP method입니다."),
    INPUT_VALUE_INVALID(400, "유효하지 않은 입력입니다."),
    INPUT_TYPE_INVALID(400, "입력 타입이 유효하지 않습니다."),
    HTTP_MESSAGE_NOT_READABLE(400, "request message body가 없거나, 값 타입이 올바르지 않습니다."),
    HTTP_HEADER_INVALID(400, "request header가 유효하지 않습니다."),
    ENTITY_NOT_FOUNT(500, "존재하지 않는 Entity입니다."),
    FORBIDDEN_ERROR(403, "작업을 수행하기 위한 권한이 없습니다."),
    IS_NOT_IMAGE(400, "이미지가 아닙니다."),
    FAILED_DURING_TRANSACTION(409, "트랜잭션 실패"),
    Json_Processing_Exception(400, "잘못된 JSON 형식 입니다."),


    // Circuit Breaker 관련 에러
    CIRCUIT_BREAKER_OPEN(503, "Circuit Breaker가 open 상태입니다. 잠시후 다시 시도해주세요"),
    RATE_LIMIT_EXCEEDED(429, "요청이 너무 많습니다. 잠시후 다시 시도해주세요"),

    // COMPANY
    COMPANY_NOT_FOUND(404, "해당 회사가 존재하지 않습니다."),

    // Event
    STAGE_NOT_FOUND(404, "해당 행사장이 존재하지 않습니다."),
    SEAT_NOT_FOUND(404, "해당 좌석이 존재하지 않습니다."),
    EVENT_NOT_FOUND(404, "해당 공연이 존재하지 않습니다."),
    EVENT_SESSION_NOT_FOUND(404, "해당 회차 공연이 존재하지 않습니다."),
    PARTITION_NOT_FOUND(404, "해당 좌석 등급 구분이 존재하지 않습니다."),
    EMPTY_EVENT_SCHEDULE(400,"회차 정보가 공백입니다."),
    ALREADY_STARTED_TICKETING(400, "이미 티켓팅이 시작 된 행사는 행사 일정 및 좌석 수정/삭제가 불가합니다"),

    //SEAT
    FAILED_TRY_ROCK(409, "락에 실패했습니다."),
    SEAT_ALREADY_RESERVED(409, "이미 선점된 좌석입니다."),
    EMPTY_SEATS_EXCEPTION(400, "선택된 좌석이 없습니다. 요청을 확인해주세요."),
    EXCEED_SEAT_RESERVATION_LIMIT(409, "1인당 예매 제한을 초과했습니다."),
    USER_ALREADY_HAS_RESERVED_SEATS(409, "사용자는 해당 이벤트에서 이미 좌석을 예약했습니다."),
    SEAT_RESERVED_BY_ANOTHER_USER(409, "해당 좌석은 다른 고객에 의해 선점됐습니다."),
    SEAT_NOT_RESERVED(409, "해당 좌석은 선점 상태가 아닙니다."),

    ;

    private final int status;
    private final String message;

}