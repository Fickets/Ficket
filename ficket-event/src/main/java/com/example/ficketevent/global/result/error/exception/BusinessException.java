package com.example.ficketevent.global.result.error.exception;

import java.util.ArrayList;
import java.util.List;

import com.example.ficketevent.global.result.error.ErrorCode;
import com.example.ficketevent.global.result.error.ErrorResponse;
import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {

    private final ErrorCode errorCode;

//    public BusinessException(String message, ErrorCode errorCode) {
//        super(message);
//        this.errorCode = errorCode;
//    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

//    public BusinessException(ErrorCode errorCode, List<ErrorResponse.FieldError> errors) {
//        super(errorCode.getMessage());
//        this.errors = errors;
//        this.errorCode = errorCode;
//    }
}