package com.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.List;

/**
 * Query 파라미터에서 JWT 토큰을 검증하고 userId를 추출해 X-User-Id 헤더에 추가하는 필터입니다.
 */
@Slf4j
@Component
public class QueryTokenExtractionFilter extends AbstractGatewayFilterFactory<QueryTokenExtractionFilter.Config> {

    private static final String TOKEN_QUERY_PARAM = "Authorization";
    private static final String BEARER_TYPE = "Bearer ";// 토큰이 포함된 Query Parameter 이름
    private static final String USER_ID_HEADER = "X-User-Id"; // 추가할 헤더 이름
    private final Key key;

    public QueryTokenExtractionFilter(@Value("${jwt.secret}") String secretKey) {
        super(Config.class);
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    public static class Config {
        // 설정 정보가 필요하면 여기에 추가
    }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // Query 파라미터에서 토큰 추출
            List<String> tokenList = request.getQueryParams().get(TOKEN_QUERY_PARAM);
            if (tokenList == null || tokenList.isEmpty()) {
                log.error("Query 파라미터에 토큰이 존재하지 않습니다.");
                return chain.filter(exchange);
            }

            // URL 디코딩
            String encodedToken = tokenList.get(0);
            String decodedToken = URLDecoder.decode(encodedToken, StandardCharsets.UTF_8);

            // Bearer 제거
            String jwt = decodedToken.startsWith(BEARER_TYPE)
                    ? decodedToken.substring(BEARER_TYPE.length())
                    : decodedToken;

            // 토큰 검증 및 userId 추출
            String userId = extractUserIdFromToken(jwt);
            if (userId != null) {
                log.info("JWT 검증 성공, userId: {}", userId);

                // userId를 X-User-Id 헤더에 추가
                ServerHttpRequest modifiedRequest = request.mutate()
                        .header(USER_ID_HEADER, userId)
                        .build();

                return chain.filter(exchange.mutate().request(modifiedRequest).build());
            }

            log.error("JWT 검증 실패 또는 userId 추출 실패");
            return chain.filter(exchange);
        };
    }

    /**
     * JWT 토큰을 검증하고 userId를 추출합니다.
     *
     * @param token JWT 토큰
     * @return userId를 String 형태로 반환, 실패 시 null
     */
    private String extractUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            // JWT 클레임에서 userId 추출
            Long userId = claims.get("userId", Long.class);
            return String.valueOf(userId);
        } catch (Exception e) {
            log.error("JWT 검증 또는 userId 추출 실패: {}", e.getMessage());
        }
        return null;
    }
}
