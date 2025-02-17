package com.example.gateway.filter;

import com.example.gateway.utils.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import static com.example.gateway.utils.JwtConstants.USER_ID_HEADER;

/**
 * JWT에서 userId를 추출해 `X-User-Id` 헤더에 추가하는 필터.
 */
@Slf4j
@Component
public class UserTokenExtractionFilter extends AbstractGatewayFilterFactory<UserTokenExtractionFilter.Config> {

    private final JwtUtil jwtUtil;

    public UserTokenExtractionFilter(JwtUtil jwtUtil) {
      super(Config.class);
      this.jwtUtil = jwtUtil;
    }

  public static class Config {
    public Config() {}
  }

    @Override
    public GatewayFilter apply(Config config) {
        return (exchange, chain) -> {
            ServerHttpRequest request = exchange.getRequest();

            // JWT 토큰 추출
            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            String jwt = jwtUtil.extractToken(authorizationHeader);

            if (jwt == null || !jwtUtil.validateToken(jwt)) {
                log.error("JWT가 없거나 유효하지 않습니다.");
                return chain.filter(exchange);
            }

            // JWT에서 userId 추출
            String userId = jwtUtil.extractUserId(jwt);
            if (userId == null) {
                log.error("JWT에서 userId를 추출하지 못했습니다.");
                return chain.filter(exchange);
            }

            log.info("JWT에서 추출된 userId={} -> {} 헤더에 추가", userId, USER_ID_HEADER);

            // userId를 헤더에 추가
            ServerHttpRequest modifiedRequest = request.mutate()
                    .header(USER_ID_HEADER, userId)
                    .build();

            return chain.filter(exchange.mutate().request(modifiedRequest).build());
        };
    }
}
