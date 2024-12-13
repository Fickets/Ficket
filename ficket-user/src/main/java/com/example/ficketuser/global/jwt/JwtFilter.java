package com.example.ficketuser.global.jwt;


import com.example.ficketuser.dto.response.CustomUserDetails;
import com.example.ficketuser.dto.response.UserDto;
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
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    private final Set<String> filterCheckUrl = new HashSet<>(
            Arrays.asList(
                    "/api/v1/users/logout",
                    "/api/v1/users/reissue",
                    "/api/v1/users/delete",
                    "/api/v1/users/additional-info",
                    "/api/v1/users/my-info"
            )
    );


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!filterCheckUrl.contains(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorization = request.getHeader("Authorization");
        log.info("TTTTTTTT", authorization);
        String access = null;
        if (authorization != null && authorization.startsWith("Bearer ")) {
            access = authorization.substring(7);
            if (jwtUtils.validateToken(access)) {
                String name = jwtUtils.getUserName(access);
                Long userId = jwtUtils.getUserId(access);
                Long socialId = jwtUtils.getSocialId(access);
                UserDto userDto = UserDto.builder()
                        .userId(userId)
                        .userName(name)
                        .socialId(socialId)
                        .build();
                CustomUserDetails customUserDetails = new CustomUserDetails(userDto);
                Authentication authToken = new UsernamePasswordAuthenticationToken(customUserDetails, null, null);
                SecurityContextHolder.getContext().setAuthentication(authToken);
                filterChain.doFilter(request, response);
            } else {

                PrintWriter writer = response.getWriter();
                writer.print("user access token expired");

                //response status code 401
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;

            }
        } else {
            log.info("ERROR NO ACCESS TOKEN");
            filterChain.doFilter(request, response);
        }


    }
}
