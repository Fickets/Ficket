package com.example.ficketuser.global.jwt;

import com.example.ficketuser.domain.dto.response.CustomUserDetails;
import com.example.ficketuser.domain.dto.response.UserDto;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private static final String[] NO_CHECK_URL = new String[]{
            "/api/v1/users/login",
            "/api/v1/users/my-ticket",
            "/actuator/**"
    };

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 특정 URL은 JWT 검사를 하지 않음
        if (Arrays.stream(NO_CHECK_URL).anyMatch(url -> matchPattern(url, request.getRequestURI()))) {
            filterChain.doFilter(request, response);
            return;
        }

        // Authorization 헤더에서 JWT 추출
        String authorization = request.getHeader("Authorization");
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            log.info("ERROR NO ACCESS TOKEN");
            filterChain.doFilter(request, response);
            return;
        }

        String accessToken = authorization.substring(7);

        // JWT 검증
        if (!jwtUtils.validateToken(accessToken)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().print("user access token expired");
            return; // ✅ 필터 중단
        }

        // JWT에서 사용자 정보 추출
        String username = jwtUtils.getUserName(accessToken);
        Long userId = jwtUtils.getUserId(accessToken);
        Long socialId = jwtUtils.getSocialId(accessToken);

        // ✅ UserDto를 사용하여 CustomUserDetails 생성
        UserDto userDto = UserDto.builder()
                .userId(userId)
                .userName(username)
                .socialId(socialId)
                .build();
        CustomUserDetails customUserDetails = new CustomUserDetails(userDto);

        // ✅ Authentication 객체 생성 시, 권한을 빈 리스트라도 추가해야 함
        Authentication authToken = new UsernamePasswordAuthenticationToken(
                customUserDetails,
                null,
                customUserDetails.getAuthorities() // ✅ 빈 리스트라도 넘겨야 오류 없음
        );

        // ✅ SecurityContextHolder에 저장
        SecurityContextHolder.getContext().setAuthentication(authToken);

        // ✅ 토큰이 유효한 경우에만 필터 실행
        filterChain.doFilter(request, response);
    }

    private boolean matchPattern(String pattern, String uri) {
        if (pattern.endsWith("/**")) {
            String basePattern = pattern.substring(0, pattern.length() - 3);
            return uri.startsWith(basePattern);
        }
        return uri.equals(pattern);
    }
}
