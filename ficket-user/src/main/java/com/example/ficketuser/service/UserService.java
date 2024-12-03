package com.example.ficketuser.service;

import com.example.ficketuser.Entity.User;
import com.example.ficketuser.Entity.UserTokenRedis;
import com.example.ficketuser.dto.UserSimpleDto;
import com.example.ficketuser.dto.response.UserDto;
import com.example.ficketuser.dto.resquest.CustomOAuth2User;
import com.example.ficketuser.global.jwt.JwtUtils;
import com.example.ficketuser.global.result.error.ErrorCode;
import com.example.ficketuser.global.result.error.exception.BusinessException;
import com.example.ficketuser.repository.UserRepository;
import com.example.ficketuser.dto.response.CustomUserDetails;
import com.example.ficketuser.dto.resquest.AdditionalInfoDto;
import com.example.ficketuser.mapper.UserMapper;
import com.example.ficketuser.repository.UserTokenRedisRepository;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final JwtUtils jwtUtils;
    private final RepositoryMetricsAutoConfiguration repositoryMetricsAutoConfiguration;

    @Value("${jwt.refresh.header}")
    private String REFRESH_HEADER;

    @Value("${jwt.access.header}")
    private String ACCESS_HEADER;


    /**
     * 카카오 로그인 유저 저장 함수
     * @param socialId
     * @param userName
     * @return
     */
    public User saveUser(Long socialId, String userName){
        User user = User.builder()
                .socialId(socialId)
                .userName(userName)
                .build();
        User saveUser = userRepository.save(user);

        return saveUser;
    }

    public User searchUser(String socialId){
       return null;
    }

    /**
     * 유저 추가정보 저장
     * @param additionalInfoDto
     * @return userSimpleDto
     */

    public UserSimpleDto additionalInfo(AdditionalInfoDto additionalInfoDto){

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));
        user.addAdditionalInfo(additionalInfoDto);

        User saveUser = userRepository.save(user);

        return userMapper.toUserSimpleDto(saveUser);
    }

    /**
     * 고객 로그아웃
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response){
        Cookie[] cookies = request.getCookies();

        // 아까 access로 등록한 유저정보 가져오기
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        // 유저 검증
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        // refresh 찾기
        String refresh = null;
        for (Cookie cookie : cookies) {
            if (cookie.getName().equals(REFRESH_HEADER)){
                refresh = cookie.getValue();
                break;
            }
        }
        if (refresh == null){
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // access, refresh ID 비교
        if (!jwtUtils.getUserId(refresh).equals(userId)){
           throw new BusinessException(ErrorCode.DIFFERENT_BOTH_TOKEN_ID);
        }
        
        // Redis Refresh 삭제
        userTokenRedisRepository.findByUserId(userId)
                .ifPresentOrElse(
                    userTokenRedisRepository::delete,
                    () -> {
                        throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
                    });
        // 쿠키 삭제
        Cookie cookie = new Cookie(REFRESH_HEADER, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);

        //TODO 로그아웃시 홈으로 보내버리기
        // response.redirect("HOME_ADDRESS") 이거 좋을듯
    }

    /**
     * 유저 ACCESS 재발급
     * @param request
     * @param response
     */
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        // Refresh 토큰 찾기
        Cookie[] cookies = request.getCookies();
        String refresh = null;
        for (Cookie cookie : cookies) {
            if(cookie != null && cookie.getName().equals(REFRESH_HEADER)){
                refresh = cookie.getValue();
            }
        }
        if (refresh == null){
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // Refresh 토큰 검증
        if (!jwtUtils.validateToken(refresh)){
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }

        // Redis Refresh와 기존 Refresh 값 비교
        Long userId = jwtUtils.getUserId(refresh);
        UserTokenRedis userTokenRedis = userTokenRedisRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NULL));

        if (!refresh.equals(userTokenRedis.getRefreshToken())) {
            throw new BusinessException(ErrorCode.DIFFERENT_REFRESH_TOKEN);
        }

        // New Access 생성
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        UserDto userDto = userMapper.toUserDto(user);
        CustomOAuth2User oAuth2User = new CustomOAuth2User(userDto);

        String newAccess = jwtUtils.createAccessToken(oAuth2User);
        response.setHeader(ACCESS_HEADER, "Bearer " + newAccess);
        response.addHeader("Access-Control-Expose-Headers", ACCESS_HEADER);

    }

    public void deleteUser(HttpServletResponse response){
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        // 유저 있으면 지우기
        userRepository.findByUserId(userId)
                .ifPresentOrElse(
                        userRepository::delete,
                        () -> { throw new BusinessException(ErrorCode.NOT_USER_FOUND); }  // 사용자 없음 시 예외 던짐
                );


        // Redis Refresh 토큰 지우기
        userTokenRedisRepository.findByUserId(userId)
                .ifPresentOrElse(
                    userTokenRedisRepository::delete,
                    () -> {throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
                });

        // 쿠키 지우기
        Cookie cookie = new Cookie(REFRESH_HEADER, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }

    public UserSimpleDto getUser(Long userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        return userMapper.toUserSimpleDto(user);
    }


}
