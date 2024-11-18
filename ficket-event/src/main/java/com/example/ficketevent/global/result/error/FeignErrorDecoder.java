package com.example.ficketevent.global.result.error;

import com.example.ficketevent.global.result.error.exception.BusinessException;
import feign.Response;
import feign.codec.ErrorDecoder;

import static com.example.ficketevent.global.result.error.ErrorCode.*;

public class FeignErrorDecoder implements ErrorDecoder {
    @Override
    public Exception decode(String methodKey, Response response) {
        switch (response.status()) {
            case 400:
                break;
            case 404:
                if (methodKey.contains("getCompany")) {
                    return new BusinessException(COMPANY_NOT_FOUND);
                }
                break;
            default:
                return new Exception(response.reason());
        }
        return null;
    }
}
