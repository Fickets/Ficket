package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

import static com.example.gateway.utils.JwtConstants.*;

/**
 * Query 파라미터에서 JWT 토큰을 검증하고 userId를 추출해 X-User-Id 헤더에 추가하는 필터입니다.
 */
@Slf4j
@Component
public class QueryTokenExtractionFilter extends AbstractGatewayFilterFactory<QueryTokenExtractionFilter.Config> {

    private final JwtUtil jwtUtil;

    public QueryTokenExtractionFilter(JwtUtil jwtUtil) {
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

            // Query 파라미터에서 JWT 추출
            List<String> tokenList = request.getQueryParams().get(TOKEN_QUERY_PARAM);
            if (tokenList == null || tokenList.isEmpty()) {
                return onError(exchange, "Query 파라미터에 Authorization 토큰이 없습니다.");
            }

            // URL 디코딩
            String encodedToken = tokenList.get(0);
            String decodedToken = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8);

            // JWT 추출 (Bearer 제거 포함)
            String jwt = jwtUtil.extractToken(decodedToken);
            if (jwt == null) {
                return onError(exchange, "JWT 토큰이 없거나 Bearer 형식이 아닙니다.");
            }

            // JWT 검증
            if (!jwtUtil.validateToken(jwt)) {
                return onError(exchange, "JWT 토큰이 유효하지 않습니다.");
            }

            // userId 추출
            String userId = jwtUtil.extractUserId(jwt);
            if (userId == null) {
                return onError(exchange, "JWT에서 userId를 추출할 수 없습니다.");
            }

            log.info("JWT 검증 성공, userId: {}", userId);

            // X-User-Id 헤더 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }


    /**
     * 에러 응답 처리 메서드
     *
     * @param exchange     ServerWebExchange 객체
     * @param errorMessage 에러 메시지
     * @return Mono<Void> 완료 신호
     */
    private Mono<Void> onError(ServerWebExchange exchange, String errorMessage) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        log.error("JWT 필터 오류: {}", errorMessage);
        return response.setComplete();
    }
}
