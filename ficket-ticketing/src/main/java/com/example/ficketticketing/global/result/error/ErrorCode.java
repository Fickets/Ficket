package com.example.ficketticketing.global.result.error;

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
    INVALID_REQUEST(400, "잘못된 요청"),
    RESOURCE_NOT_FOUND(404, "없는 요청"),
    UNKNOWN_ERROR(500, "알수없는 오류"),


    // Fegin & CircuitBreaker 관련 에러
    CIRCUIT_BREAKER_OPEN(503, "Circuit Breaker가 open 상태입니다. 잠시후 다시 시도해주세요"),
    RATE_LIMIT_EXCEEDED(429, "요청이 너무 많습니다. 잠시후 다시 시도해주세요"),
    FALLBACK_NOT_AVAILABLE(503, "OpenFeign의 Fallback이 정의되지 않았습니다."),
    FEIGN_CLIENT_ERROR(500, "Feign Client error"),
    FEIGN_SERVER_ERROR(500, "Feign Server error"),
    FEIGN_CLIENT_REQUEST_ERROR(400, "Feign Client request error"),

    // PortOne 관련 에러
    EXTERNAL_API_ERROR(500, "외부 API 오류입니다."),
    MISSING_REQUIRED_HEADERS(400, "필수 헤더가 누락되었습니다."),
    INVALID_SIGNATURE(400, "유효하지 않은 서명입니다."),
    TIMESTAMP_TOO_OLD_OR_NEW(400, "요청 타임스탬프가 너무 오래되었거나 미래의 값입니다."),
    INVALID_JSON_FORMAT(400, "잘못된 JSON 형식입니다."),
    INVALID_EVENT_TYPE(400, "잘못된 이벤트 타입입니다."),
    CANCEL_FAIL(409, "결제 취소 실패했습니다."),

    EVENT_SCHEDULE_NOT_FOUND(404, "해당 공연 회차가 없습니다."),
    USER_NOT_FOUND(404, "해당 유저가 없습니다."),
    NOT_MATCH_RESERVED_SEATS(400, "해당 유저가 선점한 좌석과 요청이 일치하지 않습니다."),

    FAILED_UPLOAD_USER_FACE(409, "사용자 얼굴 등록에 실패했습니다."),
    FAILED_DELETE_USER_FACE(409, "사용자 얼굴 삭제에 실패했습니다."),
    FAILED_SET_RELATIONSHIP_USER_FACE(409, "얼굴과 티켓의 연관관계 설정에 실패했습니다."),

    //Order
    NOT_FOUND_ORDER_PRICE(404, "해당 주문 금액을 조회할 수 없습니다."),
    NOT_MATCH_ORDER_PRICE(409, "총액이 일치하지 않습니다."),
    NOT_FOUND_ORDER_STATUS(404, "해당 주문 상태가 없습니다."),
    NOT_FOUND_ORDER(404, "해당 주문을 찾을 수 없습니다."),
    NO_MATCHING_REFUND_POLICY(409, "일치하는 환불 정책이 없습니다."),
    REFUND_FAILED(409, "티켓 환불 처리 중 오류가 발생했습니다."),
    PURCHASE_LIMIT_MET(400, "구매 제한을 충족했습니다."),

    //TICKET
    NOT_FOUND_TICKET(404, "해당 티켓을 조회할 수 없습니다"),
    ALREADY_WATCHED(409, "이미 관람한 티켓은 환불이 불가합니다."),
    ;


    private final int status;
    private final String message;

}