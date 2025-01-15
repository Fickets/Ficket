package com.example.ficketadmin.domain.admin.service;

import com.example.ficketadmin.domain.admin.dto.common.AdminDto;
import com.example.ficketadmin.domain.admin.dto.common.AdminInfoDto;
import com.example.ficketadmin.domain.admin.dto.request.AdminLoginReq;
import com.example.ficketadmin.domain.admin.dto.response.AdminLoginRes;
import com.example.ficketadmin.domain.admin.entity.Admin;
import com.example.ficketadmin.domain.admin.entity.AdminTokenRedis;
import com.example.ficketadmin.domain.admin.mapper.AdminMapper;
import com.example.ficketadmin.domain.admin.repository.AdminRepository;
import com.example.ficketadmin.domain.admin.repository.AdminTokenRedisRepository;
import com.example.ficketadmin.global.jwt.JwtUtils;
import com.example.ficketadmin.global.result.error.ErrorCode;
import com.example.ficketadmin.global.result.error.exception.BusinessException;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final AdminTokenRedisRepository adminTokenRedisRepository;
    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    private final JwtUtils jwtUtils;
    private final PasswordEncoder encoder;

    @Value("${jwt.refresh.header}")
    private String REFRESH_HEADER;

    @Value("${jwt.access.header}")
    private String ACCESS_HEADER;

    @Value("${refresh-token-maxage}")
    private int REFRESH_TOKEN_MAX_AGE;

    /**
     * 관리자 로그인
     * @param adminLoginReq
     * @return access,refresh 토큰 반환
     */
    public AdminDto login(AdminLoginReq adminLoginReq, HttpServletResponse response){


        // ID로  Admin 찾기
        Admin admin = adminRepository.findById(adminLoginReq.getId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ADMIN_FOUND));

        // PW 비교
        if(!encoder.matches(adminLoginReq.getPw(), admin.getPw())){
            throw new BusinessException(ErrorCode.DIFFERENT_PASSWORD);
        }

        AdminInfoDto adminInfo = adminMapper.toAdminInfoDto(admin);
        String accessToken = jwtUtils.createAccessToken(adminInfo);
        String refreshToken = jwtUtils.createRefreshToken(adminInfo);

        // redis에 refreshToken wjwkd
        AdminTokenRedis adminTokenRedis = AdminTokenRedis
                .builder()
                .adminId(adminInfo.getAdminId())
                .refreshToken(refreshToken)
                .build();
        adminTokenRedisRepository.save(adminTokenRedis);


        // Cookie refresh 추가
        Cookie cookie = new Cookie(REFRESH_HEADER, refreshToken);
        cookie.setMaxAge(REFRESH_TOKEN_MAX_AGE);
        cookie.setPath("/");
        response.addCookie(cookie);

        // Header에 Access TOKEN 추가
        response.setHeader(ACCESS_HEADER, "Bearer " + accessToken);
        response.setHeader("Access-Control-Expose-Headers", ACCESS_HEADER);
        AdminDto res = adminMapper.toAdminDto(admin);
        return res;
    }

    /**
     * 관리자 로그아웃
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response){

        Cookie[] cookies = request.getCookies();
        
        // Access Token 찾기
        String authorization = request.getHeader(ACCESS_HEADER);
        String access = null;
        
        if (authorization != null){
            access = authorization.substring(7);
        }
        
        // Refresh TOKEN 찾기
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

        // 양쪽 토큰 ID 비교
        Long accessId = jwtUtils.getAdminId(access);
        Long refreshId = jwtUtils.getAdminId(refresh);
        if (!accessId.equals(refreshId)){
            throw new BusinessException(ErrorCode.DIFFERENT_BOTH_TOKEN_ID);
        }

        // redis refresh token 삭제
        adminTokenRedisRepository.findByAdminId(accessId).ifPresent(adminTokenRedisRepository::delete);

        // Cookie refresh 초기화
        Cookie cookie = new Cookie(REFRESH_HEADER, null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        //  로그아웃시 홈으로 보내버리기
        // response.redirect("HOME_ADDRESS") 이거 좋을듯
    }

    /**
     * Access TOKEN 재발금
     * @param request
     * @param response
     */
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        Cookie[] cookies = request.getCookies();
        // Refresh TOKEN 찾기
        String refresh = null;
        for (Cookie cookie : cookies) {
            if(cookie.getName().equals(REFRESH_HEADER)){
                refresh = cookie.getValue();
            }
        }
        if (refresh == null){
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // refresh토큰 체크
        try {
            jwtUtils.validateToken(refresh);
        } catch (ExpiredJwtException e) {
            throw new BusinessException(ErrorCode.TOKEN_EXPIRED);
        }
        
        // Redis에 refresh 토큰 가져와서 비교
        Long refreshId = jwtUtils.getAdminId(refresh);
        AdminTokenRedis adminTokenRedis = adminTokenRedisRepository.findByAdminId(refreshId)
                .orElseThrow(() -> new BusinessException(ErrorCode.REFRESH_TOKEN_NULL));
        if(!refresh.equals(adminTokenRedis.getRefreshToken())){
            throw new BusinessException(ErrorCode.DIFFERENT_REFRESH_TOKEN);
        }

        // 토큰 생성 반환
        Admin admin = adminRepository.findByAdminId(refreshId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ADMIN_FOUND));

        AdminInfoDto adminInfo = adminMapper.toAdminInfoDto(admin);

        String newAccessToken = jwtUtils.createAccessToken(adminInfo);

        response.setHeader(ACCESS_HEADER, "Bearer " + newAccessToken);
        response.addHeader("Access-Control-Expose-Headers", "ACCESS_HEADER");
    }


    public AdminDto getAdmin(Long adminId){
        Admin admin = adminRepository.findByAdminId(adminId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_ADMIN_FOUND));

        return adminMapper.toAdminDto(admin);
    }

    public List<AdminDto> getAllAdmin() {
        return adminRepository.findAll().stream()
                .map(adminMapper::toAdminDto)
                .collect(Collectors.toList());
    }

    public List<AdminDto> getAdminsByIds(Set<Long> adminIds) {
        return adminRepository.findByAdminIdIn(adminIds).stream()
                .map(admin -> new AdminDto(admin.getAdminId(), admin.getName(), admin.getRole().name()))
                .collect(Collectors.toList());
    }

}
