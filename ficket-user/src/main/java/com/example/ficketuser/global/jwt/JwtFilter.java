package com.example.ficketuser.global.jwt;


import com.example.ficketuser.dto.response.CustomUserDetails;
import com.example.ficketuser.dto.response.UserDto;
import com.example.ficketuser.mapper.UserMapper;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.PrintWriter;

@Slf4j
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        String authorization = request.getHeader("Authorization");
        String access = null;
        if (authorization != null && authorization.startsWith("Bearer ")){
            access = authorization.substring(7);
        }else{
            log.info("ERROR NO ACCESS TOKEN");
            filterChain.doFilter(request, response);
        }

        if (jwtUtils.validateToken(access)){
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
        }else {
            log.info("NO VALIDATE TOKEN");
            return;
        }




    }
}
