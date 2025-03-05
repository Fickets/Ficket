package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT를 검증하는 필터 (유효하지 않은 경우 요청 차단).
 */
@Slf4j
@Component
public class AuthorizationHeaderFilter extends AbstractGatewayFilterFactory<AuthorizationHeaderFilter.Config> {

    private final JwtUtil jwtUtil;

    public AuthorizationHeaderFilter(JwtUtil jwtUtil) {
        super(Config.class);
        this.jwtUtil = jwtUtil;
    }

    public static class Config {
        public Config() {
        }
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            // JWT 토큰 추출
            String jwt = jwtUtil.extractToken(authorizationHeader);
            if (jwt == null) {
                return onError(exchange, "Authorization 헤더가 없거나 Bearer 타입이 아닙니다.");
            }

            // JWT 토큰 유효성 검증
            if (!jwtUtil.validateToken(jwt)) {
                return onError(exchange, "JWT 토큰이 유효하지 않습니다.");
            }

            // 유효한 토큰일 경우 필터 체인 계속
            return chain.filter(exchange);
        };
    }

    /**
     * 에러 응답 처리 메서드
     *
     * @param exchange ServerWebExchange 객체
     * @param err      에러 메시지
     * @return Mono<Void> 완료 신호
     */
    private Mono<Void> onError(ServerWebExchange exchange, String err) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error(err);
        return response.setComplete();
    }
}
