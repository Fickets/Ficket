package com.example.ficketuser.global.jwt;

import com.example.ficketuser.domain.Entity.State;
import com.example.ficketuser.domain.Entity.User;
import com.example.ficketuser.domain.Entity.UserTokenRedis;
import com.example.ficketuser.domain.dto.resquest.CustomOAuth2User;
import com.example.ficketuser.global.result.error.ErrorCode;
import com.example.ficketuser.global.result.error.exception.BusinessException;
import com.example.ficketuser.domain.repository.UserRepository;
import com.example.ficketuser.domain.repository.UserTokenRedisRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;


@Component
@Slf4j
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final UserRepository userRepository;


    private String REFRESH_HEADER;
    private String ACCESS_HEADER;
    private int REFRESH_TOKEN_MAX_AGE;

    public CustomSuccessHandler(JwtUtils jwtUtils, UserTokenRedisRepository userTokenRedisRepository, UserRepository userRepository,
                                @Value("${jwt.refresh.header}") String refreshHeader,
                                @Value("${jwt.access.header}") String accessHeader,
                                @Value("${refresh-token-maxage}") int refreshTokenMaxAge) {
        this.jwtUtils = jwtUtils;
        this.userTokenRedisRepository = userTokenRedisRepository;
        this.userRepository = userRepository;
        this.REFRESH_HEADER = refreshHeader;
        this.ACCESS_HEADER = accessHeader;
        this.REFRESH_TOKEN_MAX_AGE = refreshTokenMaxAge;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userName = customOAuth2User.getName();
        Long socialId = customOAuth2User.getSocialId();
        Long userId = customOAuth2User.getUserId();;

        String access = jwtUtils.createAccessToken(customOAuth2User);
        String refresh = jwtUtils.createRefreshToken(customOAuth2User);

        User user = userRepository.findByDeletedSocialId(socialId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));
        if(user.getState().equals(State.SUSPENDED)){
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("User is suspended.");
            response.sendRedirect("http://localhost:5173/users/suspended");
            return;
        }
        if(user.getDeletedAt() != null){
            userRepository.updateUserDeletedAt(user.getUserId());
        }

        response.addCookie(createCookie("isLogin", "true"));
        response.addCookie(createCookie(REFRESH_HEADER, refresh));
        response.setHeader(ACCESS_HEADER, "Bearer " + access);


        UserTokenRedis userTokenRedis = UserTokenRedis.builder()
                .userId(userId)
                .refreshToken(refresh)
                .build();
        userTokenRedisRepository.save(userTokenRedis);
        if (user.getGender() == null){

//            response.sendRedirect("http://localhost:5173/users/addition-info");
            response.sendRedirect("https://ficket.shop/users/addition-info");
        }else{
//            response.sendRedirect("http://localhost:5173");
            response.sendRedirect("https://ficket.shop");
        }
    }

    public Cookie createCookie(String key, String value){
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        cookie.setPath("/");
        if(key.equals(REFRESH_HEADER)){
            cookie.setHttpOnly(true);
        }
        return cookie;
    }
}