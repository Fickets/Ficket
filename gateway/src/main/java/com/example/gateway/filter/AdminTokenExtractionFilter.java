package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import static com.example.gateway.utils.JwtConstants.ADMIN_ID_HEADER;

/**
 * JWT에서 adminId를 추출해 `X-Admin-Id` 헤더에 추가하는 필터.
 */
@Slf4j
@Component
public class AdminTokenExtractionFilter extends AbstractGatewayFilterFactory<AdminTokenExtractionFilter.Config> {

    private final JwtUtil jwtUtil;

    public AdminTokenExtractionFilter(JwtUtil jwtUtil) {
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

            // JWT 토큰 추출
            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String jwt = jwtUtil.extractToken(authorizationHeader);

            if (jwt == null) {
                log.error("Authorization 헤더가 없거나 Bearer 타입이 아닙니다.");
                return chain.filter(exchange);
            }

            // `JwtUtil`을 사용하여 adminId 추출
            String adminId = jwtUtil.extractAdminId(jwt);
            if (adminId != null) {
                log.info("JWT에서 adminId 추출 성공: {}", adminId);

                // adminId를 헤더에 추가
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(ADMIN_ID_HEADER, adminId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            log.error("JWT에서 adminId를 추출하지 못했습니다.");
            return chain.filter(exchange);
        };
    }
}
