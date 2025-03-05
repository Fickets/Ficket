package com.example.ficketticketing.global.result.error;

import com.example.ficketticketing.global.result.error.exception.BusinessException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.ratelimiter.RequestNotPermitted;
import jakarta.validation.ConstraintViolationException;
import org.springframework.cloud.client.circuitbreaker.NoFallbackAvailableException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestCookieException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

import java.util.ArrayList;
import java.util.List;

import static com.example.ficketticketing.global.result.error.ErrorCode.*;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleCallNotPermittedException(CallNotPermittedException e) {
        ErrorResponse response = ErrorResponse.of(CIRCUIT_BREAKER_OPEN);
        return new ResponseEntity<>(response, HttpStatus.valueOf(CIRCUIT_BREAKER_OPEN.getStatus()));
    }

    @ExceptionHandler
    public ResponseEntity<ErrorResponse> handleRequestNotPermittedException(RequestNotPermitted e) {
        ErrorResponse response = ErrorResponse.of(RATE_LIMIT_EXCEEDED);
        return new ResponseEntity<>(response, HttpStatus.valueOf(RATE_LIMIT_EXCEEDED.getStatus()));
    }


    @ExceptionHandler(NoFallbackAvailableException.class)
    public ResponseEntity<ErrorResponse> handleNoFallbackAvailableException(NoFallbackAvailableException e) {
        // Fallback이 없는 경우 처리
        final ErrorResponse response = ErrorResponse.of(FALLBACK_NOT_AVAILABLE);
        return new ResponseEntity<>(response, HttpStatus.valueOf(FALLBACK_NOT_AVAILABLE.getStatus()));
    }

    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex) {
        ErrorResponse response = ErrorResponse.of(
                FEIGN_CLIENT_ERROR,
                ErrorResponse.FieldError.of("Feign Client Error", "", ex.getMessage())
        );
        return ResponseEntity.status(ex.status()).body(response);
    }

    @ExceptionHandler(FeignException.FeignServerException.class)
    public ResponseEntity<ErrorResponse> handleFeignServerException(FeignException.FeignServerException ex) {
        ErrorResponse response = ErrorResponse.of(
                FEIGN_SERVER_ERROR,
                ErrorResponse.FieldError.of("Feign Server Error", "", ex.getMessage())
        );
        return ResponseEntity.status(FEIGN_SERVER_ERROR.getStatus()).body(response);
    }

    @ExceptionHandler(FeignException.FeignClientException.class)
    public ResponseEntity<ErrorResponse> handleFeignNotFoundException(FeignException.FeignClientException ex) {
        ErrorResponse response = ErrorResponse.of(
                FEIGN_CLIENT_REQUEST_ERROR,
                ErrorResponse.FieldError.of("Feign Client Request Error", "", ex.getMessage())
        );
        return ResponseEntity.status(FEIGN_CLIENT_REQUEST_ERROR.getStatus()).body(response);
    }

    /**
     * 요청 헤더가 누락된 경우 처리
     */
    @ExceptionHandler(MissingRequestHeaderException.class)
    @ResponseStatus(BAD_REQUEST)
    protected ErrorResponse handleMissingRequestHeaderException(MissingRequestHeaderException e) {
        return ErrorResponse.of(INPUT_VALUE_INVALID, "Missing Header: " + e.getHeaderName());
    }


    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            //http요청 파라미터 누락
            MissingServletRequestParameterException e) {
        final ErrorResponse response = ErrorResponse.of(INPUT_VALUE_INVALID, e.getParameterName());
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException e) {  //객체 제약조건 위반
        final ErrorResponse response = ErrorResponse.of(INPUT_VALUE_INVALID,
                e.getConstraintViolations());
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBindException(
            BindException e) { //매개변수 타입 불일치 등 데이터 바인딩 실패
        final ErrorResponse response = ErrorResponse.of(INPUT_VALUE_INVALID, e.getBindingResult());
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
            //@RequestPart(파일업로드) 에서 기대한 파트 누락
            MissingServletRequestPartException e) {
        final ErrorResponse response = ErrorResponse.of(INPUT_VALUE_INVALID,
                e.getRequestPartName());
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMissingServletRequestPartException(
            //@CookieValue에서 기대한 쿠키 누락
            MissingRequestCookieException e) {
        final ErrorResponse response = ErrorResponse.of(INPUT_VALUE_INVALID, e.getCookieName());
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleMethodArgumentTypeMismatchException(
            //컨트롤러 메서드의 매개변수에 전달된 인자의 유형이 예상한 유형과 일치하지 않을 때
            MethodArgumentTypeMismatchException e) {
        final ErrorResponse response = ErrorResponse.of(e);
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {  //요청바디 역직렬화 실패
        final ErrorResponse response = ErrorResponse.of(HTTP_MESSAGE_NOT_READABLE);
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleHttpRequestMethodNotSupportedException(
            //지원하지 않는 http메서드로 요청 시
            HttpRequestMethodNotSupportedException e) {
        final List<ErrorResponse.FieldError> errors = new ArrayList<>();
        errors.add(new ErrorResponse.FieldError("http method", e.getMethod(),
                METHOD_NOT_ALLOWED.getMessage()));
        final ErrorResponse response = ErrorResponse.of(HTTP_HEADER_INVALID, errors);
        return new ResponseEntity<>(response, BAD_REQUEST);
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException e) { //기타 개발자 정의 예외
        final ErrorCode errorCode = e.getErrorCode();
        final ErrorResponse response = ErrorResponse.of(errorCode);
        return new ResponseEntity<>(response, HttpStatus.valueOf(errorCode.getStatus()));
    }

    @ExceptionHandler
    protected ResponseEntity<ErrorResponse> handleException(Exception e) {
        e.printStackTrace();
        final ErrorResponse response = ErrorResponse.of(INTERNAL_SERVER_ERROR);
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}