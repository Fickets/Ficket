package com.example.gateway.utils;

/**
 * JWT 관련 상수를 관리하는 클래스.
 */
public class JwtConstants {
    public static final String BEARER_TYPE = "Bearer ";
    public static final String USER_ID_HEADER = "X-User-Id";
    public static final String ADMIN_ID_HEADER = "X-Admin-Id";
    public static final String TOKEN_QUERY_PARAM = "Authorization";

    private JwtConstants() {
        throw new IllegalStateException("Utility class");
    }
}
