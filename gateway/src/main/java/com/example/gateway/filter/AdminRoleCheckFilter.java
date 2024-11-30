package com.example.gateway.filter;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * 이 클래스는 요청에 포함된 JWT 토큰을 검증하는 필터입니다.
 * JWT 토큰이 유효한지 검사하여 인증되지 않은 요청은 차단합니다.
 */
@Slf4j
@Component
public class AdminRoleCheckFilter extends AbstractGatewayFilterFactory<AdminRoleCheckFilter.Config> {

  private static final String BEARER_TYPE = "Bearer "; // 토큰 타입 (Bearer)
  private final Key key; // JWT 서명을 위한 키

  /**
   * AuthorizationHeaderFilter 생성자 - JWT 비밀 키를 사용해 서명 키를 생성합니다.
   *
   * @param secretKey 환경 설정에서 가져온 JWT 비밀 키
   */
  public AdminRoleCheckFilter(@Value("${jwt.secret}") String secretKey) {
    super(Config.class);
    byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
    this.key = Keys.hmacShaKeyFor(keyBytes);
  }

  public static class Config {
    // 필터에 필요한 설정 속성을 여기에 정의할 수 있습니다.
  }

  /**
   * 필터 적용 메서드 - JWT 토큰 유효성을 검사하여 유효한 경우에만 필터 체인을 통과하도록 설정합니다.
   *
   * @param config 필터 설정 정보
   * @return GatewayFilter 필터 적용 결과
   */
  @Override
  public GatewayFilter apply(Config config) {
    return (exchange, chain) -> {
      ServerHttpRequest request = exchange.getRequest();

      // Authorization 헤더가 없는 경우 오류 응답을 반환합니다.
      if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
        return onError(exchange, "Authorization 헤더가 없습니다.");
      }

      // JWT 토큰 추출 및 검증
      String authorizationHeader = request.getHeaders().get(HttpHeaders.AUTHORIZATION).get(0);
      String jwt = authorizationHeader.replace(BEARER_TYPE, "");

      if (!validateToken(jwt)) {
        return onError(exchange, "JWT 토큰이 유효하지 않습니다.");
      }

      // 토큰이 유효한 경우 다음 필터 체인으로 요청을 전달합니다.
      return chain.filter(exchange);
    };
  }

  /**
   * 오류가 발생할 경우 처리 메서드 - 응답 상태 코드를 UNAUTHORIZED로 설정하고 로그를 출력합니다.
   *
   * @param exchange ServerWebExchange 객체
   * @param err 오류 메시지
   * @return Mono<Void> 완료 신호
   */
  private Mono<Void> onError(ServerWebExchange exchange, String err) {
    ServerHttpResponse response = exchange.getResponse();
    response.setStatusCode(HttpStatus.UNAUTHORIZED);

    log.error(err); // 오류 메시지를 로그에 출력
    return response.setComplete();
  }

  /**
   * JWT 토큰의 유효성을 검사하는 메서드입니다.
   *
   * @param token 검증할 JWT 토큰
   * @return 유효한 토큰이면 true, 그렇지 않으면 false
   */
  public boolean validateToken(String token) {
    try {
      Claims claims = Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
      String role = claims.get("role").toString();
      return role.equals("MANAGER");
    } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
      log.info("유효하지 않은 JWT 토큰입니다.", e);
    } catch (ExpiredJwtException e) {
      log.info("만료된 JWT 토큰입니다.", e);
    } catch (UnsupportedJwtException e) {
      log.info("지원되지 않는 JWT 토큰입니다.", e);
    } catch (IllegalArgumentException e) {
      log.info("JWT 클레임 문자열이 비어 있습니다.", e);
    }
    return false;
  }
}
