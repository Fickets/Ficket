package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

/**
 * JWT에서 역할(Role)을 확인하여 관리자만 접근 가능하도록 하는 필터.
 */
@Slf4j
@Component
public class AdminRoleCheckFilter extends AbstractGatewayFilterFactory<AdminRoleCheckFilter.Config> {

    private final JwtUtil jwtUtil;
    private static final String REQUIRED_ROLE = "MANAGER";

    public AdminRoleCheckFilter(JwtUtil jwtUtil) {
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
            ServerHttpRequest request = exchange.getRequest();

            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String jwt = jwtUtil.extractToken(authorizationHeader);

            if (jwt == null || !jwtUtil.hasRole(jwt, REQUIRED_ROLE)) {
                return onError(exchange);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error("권한 없음: 관리자(MANAGER) 권한이 필요합니다.");
        return response.setComplete();
    }
}
