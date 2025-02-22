package com.example.ficketadmin.global.jwt;

import com.example.ficketadmin.domain.admin.dto.common.AdminInfoDto;
import com.example.ficketadmin.domain.admin.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.time.ZonedDateTime;
import java.util.Date;

@Slf4j
@Component
public class JwtUtils {

    private final Key key;
    private final long accessTokenExptime;
    private final long refreshTokenExptime;


    public JwtUtils(@Value("${jwt.secret}") String secretKey,
                    @Value("${jwt.access.expiration}") String accessTokenExptime,
                    @Value("${jwt.refresh.expiration}") String refreshTokenExptime) {
        byte[] getSecretKey = secretKey.getBytes(StandardCharsets.UTF_8);
        this.key = Keys.hmacShaKeyFor(getSecretKey);
        this.accessTokenExptime = Long.parseLong(accessTokenExptime);
        this.refreshTokenExptime = Long.parseLong(refreshTokenExptime);
    }

    /**
     * AccessToken CREATE
     *
     * @param adminInfo
     * @return AccessToken String
     */
    public String createAccessToken(AdminInfoDto adminInfo) {
        return createToken(adminInfo, accessTokenExptime);
    }

    /**
     * RefreshToken CREATE
     *
     * @param adminInfo
     * @return RefreshToken String
     */
    public String createRefreshToken(AdminInfoDto adminInfo) {
        return createToken(adminInfo, refreshTokenExptime);
    }

    /**
     * JWT CREATE
     *
     * @param adminInfo
     * @param exptime
     * @return
     */
    private String createToken(AdminInfoDto adminInfo, long exptime) {
        Claims claims = Jwts.claims();
        claims.put("adminId", adminInfo.getAdminId());
        claims.put("role", adminInfo.getRole());

        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(exptime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Admin PK GET
     *
     * @param token
     * @return Admin PK
     */
    public Long getAdminId(String token) {
        return parseClaims(token).get("adminId", Long.class);
    }

    /**
     * Admin Role GET
     *
     * @param token
     * @return Admin Role
     */
    public Role getAdminRole(String token) {
        return parseClaims(token).get("role", Role.class);
    }

    /**
     * Validate JWT
     *
     * @param token
     * @return Bool true/false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("Invalid JWT Token", e);
        } catch (ExpiredJwtException e) {
            log.info("Expired JWT Token", e);
        } catch (UnsupportedJwtException e) {
            log.info("Unsupported JWT Token", e);
        } catch (IllegalArgumentException e) {
            log.info("JWT claims string is empty.", e);
        }
        return false;
    }


    /**
     * JWT Claims GET
     *
     * @param token
     * @return JWT Claims
     */
    public Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }


}
