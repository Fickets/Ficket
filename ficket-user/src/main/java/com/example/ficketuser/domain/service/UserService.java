package com.example.ficketuser.domain.service;

import com.example.ficketuser.domain.Entity.State;
import com.example.ficketuser.domain.Entity.User;
import com.example.ficketuser.domain.Entity.UserTokenRedis;
import com.example.ficketuser.domain.client.TicketingServiceClient;
import com.example.ficketuser.domain.dto.UserSimpleDto;
import com.example.ficketuser.domain.dto.client.OrderInfoDto;
import com.example.ficketuser.domain.dto.client.TicketInfoDto;
import com.example.ficketuser.domain.dto.response.*;
import com.example.ficketuser.domain.dto.resquest.CustomOAuth2User;
import com.example.ficketuser.domain.dto.resquest.CustomerReq;
import com.example.ficketuser.domain.dto.resquest.UpdateUserRequest;
import com.example.ficketuser.global.jwt.JwtUtils;
import com.example.ficketuser.global.result.error.ErrorCode;
import com.example.ficketuser.global.result.error.exception.BusinessException;
import com.example.ficketuser.global.utils.CircuitBreakerUtils;
import com.example.ficketuser.domain.mapper.TicketMapper;
import com.example.ficketuser.domain.repository.UserCustomRepository;
import com.example.ficketuser.domain.repository.UserRepository;
import com.example.ficketuser.domain.dto.resquest.AdditionalInfoDto;
import com.example.ficketuser.domain.mapper.UserMapper;
import com.example.ficketuser.domain.repository.UserTokenRedisRepository;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.autoconfigure.metrics.data.RepositoryMetricsAutoConfiguration;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final UserTokenRedisRepository userTokenRedisRepository;
    private final JwtUtils jwtUtils;
    private final TicketingServiceClient ticketingServiceClient;
    private final TicketMapper ticketMapper;
    private final CircuitBreakerRegistry circuitBreakerRegistry;
    private final UserCustomRepository userCustomRepository;

    @Value("${jwt.refresh.header}")
    private String REFRESH_HEADER;

    @Value("${jwt.access.header}")
    private String ACCESS_HEADER;


    /**
     * 카카오 로그인 유저 저장 함수
     *
     * @param socialId
     * @param userName
     * @return
     */
    public User saveUser(Long socialId, String userName) {
        User user = User.builder()
                .socialId(socialId)
                .userName(userName)
                .state(State.ACTIVE)
                .build();
        User saveUser = userRepository.save(user);

        return saveUser;
    }

    public User searchUser(String socialId) {
        return null;
    }

    /**
     * 유저 추가정보 저장
     *
     * @param additionalInfoDto
     * @return userSimpleDto
     */

    public UserSimpleDto additionalInfo(AdditionalInfoDto additionalInfoDto) {

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));
        user.addAdditionalInfo(additionalInfoDto);
        user.setState(State.ACTIVE);
        User saveUser = userRepository.save(user);

        return userMapper.toUserSimpleDto(saveUser);
    }

    /**
     * 고객 로그아웃
     *
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {
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
            if (cookie.getName().equals(REFRESH_HEADER)) {
                refresh = cookie.getValue();
                break;
            }
        }
        // 서버가 꺼지고 켜지면 날라가서 일단 주석 처리
//        if (refresh == null) {
//            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
//        }

        // access, refresh ID 비교
        if (!jwtUtils.getUserId(refresh).equals(userId)) {
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
        Cookie cookie2 = new Cookie("isLogin", "false");
        cookie2.setMaxAge(1209600000); // 2주
        cookie2.setPath("/");
        response.addCookie(cookie2);
        // 로그아웃시 홈으로 보내버리기
        // response.redirect("HOME_ADDRESS") 이거 좋을듯
    }

    /**
     * 유저 ACCESS 재발급
     *
     * @param request
     * @param response
     */
    public void reissue(HttpServletRequest request, HttpServletResponse response) {

        // Refresh 토큰 찾기
        Cookie[] cookies = request.getCookies();
        String refresh = null;
        for (Cookie cookie : cookies) {
            if (cookie != null && cookie.getName().equals(REFRESH_HEADER)) {
                refresh = cookie.getValue();
            }
        }
        if (refresh == null) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
        }

        // Refresh 토큰 검증
        if (!jwtUtils.validateToken(refresh)) {
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

    @Transactional
    public void deleteUser(HttpServletResponse response) {
        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();


        // 유저 있으면 지우기
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() ->  new BusinessException(ErrorCode.NOT_USER_FOUND));

        List<TicketInfoDto> ticketInfoDtoList = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getMyTicketCircuitBreaker2",
                () -> ticketingServiceClient.getMyTickets(user.getUserId())
        );

        if (!ticketInfoDtoList.isEmpty()){
            throw new BusinessException(ErrorCode.EXIST_USER_EVENT);
        }

        // Redis Refresh 토큰 지우기
        userTokenRedisRepository.findByUserId(userId)
                .ifPresentOrElse(
                        userTokenRedisRepository::delete,
                        () -> {
                            throw new BusinessException(ErrorCode.REFRESH_TOKEN_NULL);
                        });

        // 쿠키 지우기
        Cookie cookie = new Cookie(REFRESH_HEADER, "");
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
        Cookie cookie2 = new Cookie("isLogin", "false");
        cookie2.setMaxAge(1209600000); // 2주
        cookie2.setPath("/");
        response.addCookie(cookie2);

        userRepository.delete(user);

    }

    public UserSimpleDto getUser(Long userId) {
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        return userMapper.toUserSimpleDto(user);
    }

    /**
     *
     * @param updateUserRequest 유저 변경 정보
     */
    @Transactional
    public void updateUser(UpdateUserRequest updateUserRequest) {
        User user = userRepository.findByUserId(updateUserRequest.getUserId())
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        user.updateUserInfo(updateUserRequest.getUserName(),updateUserRequest.getBirth(),updateUserRequest.getGender());
    }

    /**
     * 사용자 티켓 조회 서비스
     *
     * @param userId 사용자 ID
     * @param page   페이지 번호
     * @param size   페이지 크기
     * @param sort   정렬 기준 ("asc" 또는 "desc")
     * @param sidoFilter 시/도 필터 (null일 경우 필터링 없음)
     * @return 페이징된 티켓 리스트
     */
    public PagedResponse<MyTicketResponse> getMyTicket(Long userId, int page, int size, String sort, String sidoFilter) {
        // 사용자 조회
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        // CircuitBreaker를 사용하여 외부 서비스 호출
//        List<TicketInfoDto> ticketInfoDtoList = CircuitBreakerUtils.executeWithCircuitBreaker(
//                circuitBreakerRegistry,
//                "getMyTicketCircuitBreaker",
//                () -> ticketingServiceClient.getMyTickets(user.getUserId())
//        );
        List<TicketInfoDto> ticketInfoDtoList = ticketingServiceClient.getMyTickets(user.getUserId());


        // 시/도 필터 적용
        if (sidoFilter != null && !sidoFilter.isBlank()) {
            ticketInfoDtoList = ticketInfoDtoList.stream()
                    .filter(ticket -> ticket.getSido().equalsIgnoreCase(sidoFilter))
                    .toList();
        }

        // 이벤트별로 그룹화
        Map<Long, List<TicketInfoDto>> groupedByEvent = ticketInfoDtoList.stream()
                .collect(Collectors.groupingBy(TicketInfoDto::getOrderId));

        // 그룹화된 데이터를 MyTicketResponse로 변환
        List<MyTicketResponse> allResponses = groupedByEvent.values().stream()
                .map(this::convertGroupedTicketsToMyTicketResponse)
                .toList();

        // createdAt 정렬 (기본값: 오름차순, desc일 경우 내림차순)
        if ("desc".equalsIgnoreCase(sort)) {
            allResponses = allResponses.stream()
                    .sorted(Comparator.comparing(MyTicketResponse::getCreatedAt).reversed())
                    .toList();
        } else {
            allResponses = allResponses.stream()
                    .sorted(Comparator.comparing(MyTicketResponse::getCreatedAt))
                    .toList();
        }

        // 페이징 처리
        Pageable pageable = PageRequest.of(page, size);
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allResponses.size());

        // 페이징된 결과 리스트 반환
        List<MyTicketResponse> paginatedResponses = start > allResponses.size()
                ? Collections.emptyList()
                : allResponses.subList(start, end);

        // 총 페이지 수 계산
        int totalPages = (int) Math.ceil((double) allResponses.size() / size);

        // PagedResponse 객체 반환
        return new PagedResponse<>(
                paginatedResponses,
                page,
                size,
                allResponses.size(),
                totalPages
        );
    }


    /**
     * 그룹화된 티켓 리스트를 MyTicketResponse로 변환
     *
     * @param tickets 티켓 리스트
     * @return 변환된 MyTicketResponse 객체
     */
    private MyTicketResponse convertGroupedTicketsToMyTicketResponse(List<TicketInfoDto> tickets) {
        // 그룹 내 첫 번째 티켓 정보를 가져옴
        TicketInfoDto firstTicket = tickets.get(0);

        // 좌석 정보 리스트 생성
        List<MySeatInfo> mySeatInfoList = tickets.stream()
                .map(ticketMapper::toMySeatInfo)
                .toList();

        // MyTicketResponse로 변환
        MyTicketResponse response = ticketMapper.toMyTicketResponse(firstTicket);
        response.setMySeatInfoList(mySeatInfoList);

        return response;
    }

    public List<UserSimpleDto> getTicketingUsers(List<Long> userIds){
        List<UserSimpleDto> res = new ArrayList<>();
        for (Long userId : userIds) {
            userRepository.findByUserId(userId)
                    .ifPresent(user -> res.add(userMapper.toUserSimpleDto(user)));
        }

        return res;
    }

    public UserSimpleDto getMyInfo(){

        CustomUserDetails userDetails = (CustomUserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Long userId = userDetails.getUserId();

        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        return userMapper.toUserSimpleDto(user);
    }

    public List<String> getAllUser(){
        List<String> res = userRepository.findAll().stream()
                .map(user -> user.getUserName())
                .toList();
        return res;
    }
    public PagedResponse<UserSimpleDto> getCustomerSearch(CustomerReq customerReq, Pageable pageable){
        List<UserSimpleDto> results = userCustomRepository.getUserPage(customerReq, pageable);

        // 페이징 처리
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), results.size());
        List<UserSimpleDto> pagedResults = results.subList(start, end);

        return new PagedResponse<>(
                pagedResults,
                pageable. getPageNumber(),
                pageable.getPageSize(),
                results.size(),
                (int) Math.ceil((double) results.size() / pageable.getPageSize())
        );
    }

    public List<OrderInfoDto> getCustomerTicket(Long userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        List<OrderInfoDto> res = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getCustomerTicket",
                () -> ticketingServiceClient.getCustomerTicket(userId));
        return res;
    }

    public void customerDelete(Long userId){
        User user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_USER_FOUND));

        List<OrderInfoDto> ticketInfoDtoList = CircuitBreakerUtils.executeWithCircuitBreaker(
                circuitBreakerRegistry,
                "getCustomerOrderTicket",
                () -> ticketingServiceClient.getCustomerTicket(user.getUserId())
        );
        if (!ticketInfoDtoList.isEmpty()){
            throw new BusinessException(ErrorCode.EXIST_USER_EVENT);
        }
        userRepository.customerForceDelete(user.getUserId());
    }



}
