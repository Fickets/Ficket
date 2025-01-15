package com.example.ficketuser.global.config.fegin;

import feign.Target;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.CircuitBreakerNameResolver;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Feign 클라이언트를 위한 사용자 정의 Circuit Breaker 이름 생성기.
 * 호출되는 대상 URL의 호스트와 메서드 이름을 기반으로 Circuit Breaker 이름을 생성합니다.
 */
@Slf4j
@Component
public class CustomCircuitBreakerNameResolver implements CircuitBreakerNameResolver {

    /**
     * 특정 Feign 클라이언트와 메서드 호출에 대해 Circuit Breaker 이름을 생성합니다.
     *
     * @param feignClientName Feign 클라이언트의 이름.
     * @param target          Feign 클라이언트 대상(프록시 객체).
     * @param method          호출된 Feign 메서드.
     * @return 생성된 Circuit Breaker 이름.
     */
    @Override
    public String resolveCircuitBreakerName(String feignClientName, Target<?> target, Method method) {
        String url = target.url(); // Feign 클라이언트 대상의 기본 URL 가져오기
        try {
            // URL에서 호스트 이름 추출 (예: http://ticketing-service → ticketing-service)
            String host = new URL(url).getHost();

            // 호출된 메서드 이름 가져오기 (예: getMyTickets)
            String methodName = method.getName();

            // Circuit Breaker 이름 생성 ("cb_<호스트>_<메서드>" 형식으로)
            return String.format("cb_%s_%s", host, methodName);
        } catch (MalformedURLException e) {
            // URL 형식이 잘못된 경우 오류 로그를 남김
            log.error("MalformedURLException 발생: {}", url, e);

            // 기본 Circuit Breaker 이름 반환
            return "cb_default";
        }
    }
}
