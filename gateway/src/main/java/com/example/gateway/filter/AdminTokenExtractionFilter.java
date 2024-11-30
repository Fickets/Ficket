package com.example.gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT에서 값 추출 후 헤더에 추가하는 필터입니다.
 */
@Slf4j
@Component
public class AdminTokenExtractionFilter extends AbstractGatewayFilterFactory<AdminTokenExtractionFilter.Config> {

  private static final String BEARER_TYPE = "Bearer ";
  private static final String ADMIN_ID_HEADER = "X-Admin-Id"; // 추가할 헤더 이름
  private final Key key;

  public AdminTokenExtractionFilter(@Value("${jwt.secret}") String secretKey) {
    super(Config.class);
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public static class Config {
    // 필터에 필요한 설정 속성을 여기에 정의할 수 있습니다.
  }

  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      // Authorization 헤더 확인
      String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
      if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER_TYPE)) {
        log.error("Authorization 헤더가 없거나 Bearer 타입이 아닙니다.");
        return chain.filter(exchange);
      }

      // JWT 토큰 추출
      String jwt = authorizationHeader.replace(BEARER_TYPE, "");

      // JWT에서 adminId 추출 및 변환
      String adminId = extractAdminIdAsString(jwt);
      if (adminId != null) {
        // adminId를 헤더에 추가
        ServerHttpRequest modifiedRequest = request.mutate()
                .header(ADMIN_ID_HEADER, adminId) // Long 값을 문자열로 변환하여 추가
                .build();

        return chain.filter(exchange.mutate().request(modifiedRequest).build());
      }

      log.error("JWT에서 adminId를 추출하지 못했습니다.");
      return chain.filter(exchange);
    };
  }

  /**
   * JWT에서 adminId를 추출하고 Long 타입으로 변환합니다.
   *
   * @param token JWT 토큰
   * @return adminId를 Long으로 반환, 실패 시 null
   */
  private String extractAdminIdAsString(String token) {
    try {
      Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      Long adminId = claims.get("adminId", Long.class);
      return String.valueOf(adminId);
    } catch (NumberFormatException e) {
      log.error("adminId를 Long으로 변환할 수 없습니다.", e);
    } catch (Exception e) {
      log.error("JWT에서 adminId 추출 실패", e);
    }
    return null;
  }
}
