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


@Slf4j
@Component
public class CustomSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtUtils jwtUtils;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final UserRepository userRepository;


    private final String REFRESH_HEADER;
    private final String ACCESS_HEADER;
    private final int REFRESH_TOKEN_MAX_AGE;
    private final String REDIRECT_URL;
    private final String SUSPENDED_URL;
    private final String ADDITIONAL_INFO_URL;


    public CustomSuccessHandler(JwtUtils jwtUtils, UserTokenRedisRepository userTokenRedisRepository, UserRepository userRepository,
                                @Value("${jwt.refresh.header}") String refreshHeader,
                                @Value("${jwt.access.header}") String accessHeader,
                                @Value("${refresh-token-maxage}") int refreshTokenMaxAge,
                                @Value("${login.redirect-url}") String redirectUrl,
                                @Value("${login.suspended-url}") String suspendedUrl,
                                @Value("${login.additional-info-url}") String additionalInfoUrl
    ) {
        this.jwtUtils = jwtUtils;
        this.userTokenRedisRepository = userTokenRedisRepository;
        this.userRepository = userRepository;
        this.REFRESH_HEADER = refreshHeader;
        this.ACCESS_HEADER = accessHeader;
        this.REFRESH_TOKEN_MAX_AGE = refreshTokenMaxAge;
        this.REDIRECT_URL = redirectUrl;
        this.SUSPENDED_URL = suspendedUrl;
        this.ADDITIONAL_INFO_URL = additionalInfoUrl;
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException {

        CustomOAuth2User customOAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        String userName = customOAuth2User.getName();
        Long socialId = customOAuth2User.getSocialId();
        Long userId = customOAuth2User.getUserId();
        log.info("HERE : 1");

        String access = jwtUtils.createAccessToken(customOAuth2User);
        log.info("HERE : 2");
        String refresh = jwtUtils.createRefreshToken(customOAuth2User);
        log.info("HERE : 3");
        User user = userRepository.findByDeletedSocialId(socialId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));
        log.info("HERE : 4");
        if (user.getState().equals(State.SUSPENDED)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.getWriter().write("User is suspended.");
            response.sendRedirect(SUSPENDED_URL);
            return;
        }
        log.info("HERE : 5");
        if (user.getDeletedAt() != null) {
            userRepository.updateUserDeletedAt(user.getUserId());
        }
        log.info("HERE : 6");
        response.addCookie(createCookie("isLogin", "true"));
        response.addCookie(createCookie(REFRESH_HEADER, refresh));
        response.setHeader(ACCESS_HEADER, "Bearer " + access);
        log.info("HERE : 7");
        log.info("Access Token: Bearer {}", access);
        log.info("Refresh Token: Bearer {}", refresh);
        log.info("HERE : 8");
        UserTokenRedis userTokenRedis = UserTokenRedis.builder()
                .userId(userId)
                .refreshToken(refresh)
                .build();
        userTokenRedisRepository.save(userTokenRedis);
        if (user.getGender() == null) {
            log.info("HERE : 9-1");
            response.sendRedirect(ADDITIONAL_INFO_URL);
        } else {
            log.info("HERE : 9-2");
            response.sendRedirect(REDIRECT_URL);
        }
    }

    public Cookie createCookie(String key, String value) {
        Cookie cookie = new Cookie(key, value);
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        cookie.setPath("/");
        cookie.setSecure(true);
        cookie.setDomain("ficket.shop");
        cookie.setHttpOnly(false);
        return cookie;
    }
}