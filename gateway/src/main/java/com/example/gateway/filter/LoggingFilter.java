package com.example.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.OrderedGatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

/**
 * 이 클래스는 Spring Cloud Gateway에서 요청 및 응답을 로깅하는 필터입니다.
 * 요청 전에 로깅할지, 응답 후에 로깅할지 설정할 수 있으며,
 * 기본 메시지를 추가하여 로깅 정보를 제공할 수 있습니다.
 */
@Slf4j
@Component
public class LoggingFilter extends AbstractGatewayFilterFactory<LoggingFilter.Config> {

    // 기본 생성자 - Config 클래스의 설정 정보를 사용할 수 있도록 함
    public LoggingFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // 필터 적용 - OrderedGatewayFilter를 사용하여 우선순위 설정 가능
        return new OrderedGatewayFilter((exchange, chain) -> {
            // 요청 및 응답 객체를 가져옴
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 설정된 기본 메시지를 로그에 출력
            log.info("로깅 필터 기본 메시지: {}", config.getBaseMessage());

            // Pre-Logger가 활성화된 경우, 요청 ID를 로그에 출력
            if (config.isPreLogger()) {
                log.info("로깅 PRE 필터: 요청 ID -> {}", request.getId());
            }

            // 체인 필터 실행 후 응답 코드 로깅 (Post-Logger가 활성화된 경우)
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("로깅 POST 필터: 응답 코드 -> {}", response.getStatusCode());
                }
            }));
        }, Ordered.LOWEST_PRECEDENCE);
    }

    // 필터 설정 정보 클래스 (Config) 정의
    @Data
    public static class Config {
        private String baseMessage; // 기본 메시지
        private boolean preLogger; // 요청 전에 로깅할지 여부
        private boolean postLogger; // 응답 후에 로깅할지 여부
    }
}
