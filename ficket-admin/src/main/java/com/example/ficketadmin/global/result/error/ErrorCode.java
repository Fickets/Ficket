package com.example.ficketadmin.global.result.error;

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

    // Event
    COMPANY_NOT_FOUND(404, "해당 회사가 존재하지 않습니다."),

    // Admin
    NOT_ADMIN_FOUND(404, "해당 관리자가 존재하지 않습니다."),
    DIFFERENT_PASSWORD(401, "틀린 비밀번호 입니다."),
    REFRESH_TOKEN_NULL(404, "REFRESH_TOKEN이 없습니다."),
    DIFFERENT_BOTH_TOKEN_ID(400, "양쪽 토큰 ID값이 다릅니다."),
    REFRESH_TOKEN_EXPIRED(400, "토큰이 만료되었습니다."),
    DIFFERENT_REFRESH_TOKEN(400, "등록되지 않은 REFRESH_TOKEN 입니다."),
    ;

    private final int status;
    private final String message;

}