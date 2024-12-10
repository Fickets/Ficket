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
        switch (response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getEventSchedule")) {
                    return new BusinessException(EVENT_SCHEDULE_NOT_FOUND);
                } else if (methodKey.contains("getUser")) {
                    return new BusinessException(USER_NOT_FOUND);
                }
                break;
            default:
                return new Exception(response.reason());
        }
        return null;
    }
}
