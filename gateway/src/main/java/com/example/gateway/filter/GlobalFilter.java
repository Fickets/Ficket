package com.example.gateway.filter;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;


/**
 * 이 클래스는 Spring Cloud Gateway의 글로벌 필터로,
 * 요청 및 응답에 대한 기본 로깅을 수행합니다.
 * <p>
 * 필터 설정(Config)으로 요청 전후에 로깅할지 여부를 설정할 수 있습니다.
 * 'baseMessage' 필드를 통해 로그에 포함할 기본 메시지를 지정할 수 있습니다.
 * <p>
 * 요청 ID와 응답 코드를 로깅하여 트래픽 추적에 유용하게 사용할 수 있습니다.
 */
@Slf4j
@Component
public class GlobalFilter extends AbstractGatewayFilterFactory<GlobalFilter.Config> {

    // 기본 생성자 - Config 클래스의 설정 정보를 사용하도록 설정
    public GlobalFilter() {
        super(Config.class);
    }

    @Override
    public GatewayFilter apply(Config config) {
        // 필터 로직 정의 - 요청과 응답을 로깅할 수 있도록 설정
        return ((exchange, chain) -> {
            // 요청 및 응답 객체를 가져옴
            ServerHttpRequest request = exchange.getRequest();
            ServerHttpResponse response = exchange.getResponse();

            // 기본 메시지와 요청 IP 주소를 로그에 출력
            log.info("글로벌 필터 기본 메시지: {}, 요청 IP 주소: {}", config.getBaseMessage(), request.getRemoteAddress());

            // Pre-Logger가 활성화된 경우 요청 ID를 로그에 출력
            if (config.isPreLogger()) {
                log.info("글로벌 필터 시작: 요청 ID -> {}", request.getId());
            }

            // 체인 필터 실행 후 응답 코드 로깅 (Post-Logger가 활성화된 경우)
            return chain.filter(exchange).then(Mono.fromRunnable(() -> {
                if (config.isPostLogger()) {
                    log.info("글로벌 필터 종료: 응답 코드 -> {}", response.getStatusCode());
                }
            }));
        });
    }

    // 필터 설정 정보 클래스 (Config) 정의
    @Data
    public static class Config {
        private String baseMessage; // 기본 메시지
        private boolean preLogger; // 요청 전에 로깅할지 여부
        private boolean postLogger; // 응답 후에 로깅할지 여부
    }
}
