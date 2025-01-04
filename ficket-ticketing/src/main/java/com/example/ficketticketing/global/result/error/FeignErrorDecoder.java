package com.example.ficketticketing.global.result.error;

import com.example.ficketticketing.global.result.error.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;
import lombok.extern.slf4j.Slf4j;

import static com.example.ficketticketing.global.result.error.ErrorCode.*;

@Slf4j
public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        log.error("Feign client error: methodKey={}, status={}, reason={}",
                methodKey, response.status(), response.reason());

        // 특정 메서드의 에러 처리 추가
        if (methodKey.contains("settingRelationship")) {
            // 200번대가 아닌 경우 오류 처리
            if (response.status() < 200 || response.status() >= 300) {
                return new BusinessException(FAILED_SET_RELATIONSHIP_USER_FACE);
            }
        } else if (methodKey.contains("uploadFace")) {
            if (response.status() < 200 || response.status() >= 300) {
                return new BusinessException(FAILED_UPLOAD_USER_FACE);
            }
        } else if (methodKey.contains("deleteFace")) {
            if (response.status() < 200 || response.status() >= 300) {
                return new BusinessException(FAILED_DELETE_USER_FACE);
            }
        }

        switch (response.status()) {
            case 400:
                log.error("Bad Request for methodKey={} with status={} and reason={}", methodKey, response.status(), response.reason());

                return new BusinessException(INVALID_REQUEST); // 적절한 ErrorCode 추가
            case 404:
                if (methodKey.contains("getEventSchedule")) {
                    return new BusinessException(EVENT_SCHEDULE_NOT_FOUND);
                } else if (methodKey.contains("getUser")) {
                    return new BusinessException(USER_NOT_FOUND);
                }
                return new BusinessException(RESOURCE_NOT_FOUND); // 기본 404 처리
            default:
                return new BusinessException(UNKNOWN_ERROR); // 기본 처리
        }
    }
}
