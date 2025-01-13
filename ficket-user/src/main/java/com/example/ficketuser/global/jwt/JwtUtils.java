package com.example.ficketuser.global.jwt;


import com.example.ficketuser.domain.dto.resquest.CustomOAuth2User;
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
     * @param customOAuth2User
     * @return AccessToken String
     */
    public String createAccessToken(CustomOAuth2User customOAuth2User) {
        return createToken(customOAuth2User, accessTokenExptime);
    }

    /**
     * RefreshToken CREATE
     * @param customOAuth2User
     * @return RefreshToken String
     */
    public String createRefreshToken(CustomOAuth2User customOAuth2User) {
        return createToken(customOAuth2User, refreshTokenExptime);
    }

    /**
     * JWT CREATE
     * @param customOAuth2User
     * @param exptime
     * @return
     */
    private String createToken(CustomOAuth2User customOAuth2User, long exptime) {
        Claims claims = Jwts.claims();
        claims.put("userId", customOAuth2User.getUserId());
        claims.put("userName", customOAuth2User.getName());
        claims.put("socialId", customOAuth2User.getSocialId());


        ZonedDateTime now = ZonedDateTime.now();
        ZonedDateTime tokenValidity = now.plusSeconds(604800000);
//        ZonedDateTime tokenValidity = now.plusSeconds(exptime);

        return Jwts.builder()
                .setClaims(claims)
                .setIssuedAt(Date.from(now.toInstant()))
                .setExpiration(Date.from(tokenValidity.toInstant()))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * User PK GET
     * @param token
     * @return User PK
     */
    public Long getUserId(String token) {
        return parseClaims(token).get("userId", Long.class);
    }

    /**
     * User userName GET
     * @param token
     * @return userName
     */
    public String getUserName(String token) {
        return parseClaims(token).get("userName", String.class);
    }

    /**
     * User socialId GET
     * @param token
     * @return socialId
     */
    public Long getSocialId(String token) {
        return parseClaims(token).get("socialId", Long.class);
    }


    /**
     * Validate JWT
     * @param token
     * @return Bool true/false
     */
    public boolean validateToken(String token) {
        try{
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
