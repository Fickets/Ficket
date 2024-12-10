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

        switch (response.status()) {
            case 400:
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
