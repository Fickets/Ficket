package com.example.ficketadmin.global.jwt;

import com.example.ficketadmin.domain.admin.dto.common.CustomAdminDetails;
import com.example.ficketadmin.domain.admin.service.CustomAdminDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAdminFilter extends OncePerRequestFilter {

    private final CustomAdminDetailsService customAdminDetailsService;
    private final JwtUtils jwtUtils;

    /**
     * JWT  토큰 검사
     *
     * @param request
     * @param response
     * @param filterChain
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (request.getRequestURI().equals("/api/v1/admins/login")) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");

        // JWT HEADER 검사
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            String token = authorizationHeader.substring(7);

            // JWT 유효성 검사
            if (jwtUtils.validateToken(token)) {
                Long adminId = jwtUtils.getAdminId(token);
                // 해당 유저 adminDetails 생성
                UserDetails adminDetails = customAdminDetailsService.loadUserByUsername(adminId.toString());

                if (adminDetails != null) {
                    // AdminDetails로 접근권한 인증 TOKEN 생성
                    UsernamePasswordAuthenticationToken adminAuthenticationToken =
                            new UsernamePasswordAuthenticationToken(adminDetails, null, adminDetails.getAuthorities());
                    // 이번 Request Security Context에 접근권하 설정
                    SecurityContextHolder.getContext().setAuthentication(adminAuthenticationToken);
                }

            }

        }
        // 다음 넘어가기
        filterChain.doFilter(request, response);
    }
}
