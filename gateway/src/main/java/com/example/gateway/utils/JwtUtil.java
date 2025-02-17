package com.example.gateway.utils;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;

/**
 * JWT 관련 기능을 제공하는 유틸 클래스.
 */
@Slf4j
@Component
public class JwtUtil {

    private final Key secretKey;

    /**
     * JWT 비밀 키를 사용해 서명 키를 생성합니다.
     *
     * @param secretKey 환경 변수에서 불러온 JWT 비밀 키
     */
    public JwtUtil(@Value("${jwt.secret}") String secretKey) {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * JWT 토큰을 검증합니다.
     *
     * @param token 검증할 JWT 토큰
     * @return 유효하면 true, 그렇지 않으면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.warn("유효하지 않은 JWT 토큰입니다.", e);
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.warn("JWT 클레임이 비어 있습니다.", e);
        }
        return false;
    }

    /**
     * JWT에서 `userId`를 추출합니다.
     *
     * @param token JWT 토큰
     * @return userId 값 (String)
     */
    public String extractUserId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            Long userId = claims.get("userId", Long.class);
            return String.valueOf(userId);
        } catch (Exception e) {
            log.warn("JWT에서 userId 추출 실패", e);
        }
        return null;
    }

    /**
     * JWT에서 `adminId`를 추출합니다.
     *
     * @param token JWT 토큰
     * @return adminId 값 (String)
     */
    public String extractAdminId(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            Long adminId = claims.get("adminId", Long.class);
            return String.valueOf(adminId);
        } catch (Exception e) {
            log.warn("JWT에서 adminId 추출 실패", e);
        }
        return null;
    }

    /**
     * JWT에서 `role` 값을 추출합니다.
     *
     * @param token JWT 토큰
     * @return role 값 (String)
     */
    public String extractUserRole(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();

            return claims.get("role", String.class);
        } catch (Exception e) {
            log.warn("JWT에서 role 추출 실패", e);
        }
        return null;
    }

    /**
     * Authorization 헤더에서 JWT 토큰을 추출합니다.
     *
     * @param authorizationHeader Authorization 헤더 값
     * @return JWT 토큰 (Bearer 제거 후)
     */
    public String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith(JwtConstants.BEARER_TYPE)) {
            return authorizationHeader.replace(JwtConstants.BEARER_TYPE, "").trim();
        }
        return null;
    }

    /**
     * 사용자의 역할이 특정 역할과 일치하는지 확인합니다.
     *
     * @param token JWT 토큰
     * @param requiredRole 요구되는 역할
     * @return 사용자가 해당 역할을 가지고 있으면 true, 그렇지 않으면 false
     */
    public boolean hasRole(String token, String requiredRole) {
        String role = extractUserRole(token);
        return requiredRole.equals(role);
    }
}
