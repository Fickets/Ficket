package com.example.ficketuser.controller;

import com.example.ficketuser.dto.UserSimpleDto;
import com.example.ficketuser.dto.response.MyTicketResponse;
import com.example.ficketuser.dto.response.PagedResponse;
import com.example.ficketuser.dto.response.UserDto;
import com.example.ficketuser.dto.resquest.AdditionalInfoDto;
import com.example.ficketuser.dto.resquest.UpdateUserRequest;
import com.example.ficketuser.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    /**
     * 회원가입 추가 정보 입력 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @PostMapping("/additional-info")
    public ResponseEntity<UserSimpleDto> addUserInfo(@RequestBody AdditionalInfoDto additionalInfoDto) {

        UserSimpleDto userSimpleDto = userService.additionalInfo(additionalInfoDto);

        return ResponseEntity.ok(userSimpleDto);
    }

    /**
     * 유저 로그아웃 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @GetMapping("/logout")
    public ResponseEntity<Integer> logout(HttpServletRequest request, HttpServletResponse response) {
        userService.logout(request, response);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 유저 ACCESS 재발급 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @GetMapping("/reissue")
    public ResponseEntity<Integer> reissue(HttpServletRequest request, HttpServletResponse response) {
        userService.reissue(request, response);
        return ResponseEntity.status(HttpStatus.OK).build();
    }


    /**
     * 유저 회원탈퇴
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @DeleteMapping("/delete")
    public ResponseEntity<Integer> deleteUser(HttpServletResponse response) {
        userService.deleteUser(response);
        return null;
    }


    /**
     * 유저 PK 유저 조회
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserSimpleDto> getUser(@PathVariable Long userId) {

        UserSimpleDto res = userService.getUser(userId);

        return ResponseEntity.ok(res);
    }

    /**
     * 유저 정보 수정
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-10
     * 변경 이력:
     * - 2024-12-10 오형상: 초기 작성
     */
    @PutMapping
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest updateUserRequest) {

        userService.updateUser(updateUserRequest);

        return ResponseEntity.noContent().build();
    }


    /**
     * 마이 티켓 조회
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-12-09
     * 변경 이력:
     * - 2024-12-09 오형상: 초기 작성
     */
    @GetMapping("/my-ticket")
    public ResponseEntity<PagedResponse<MyTicketResponse>> getMyTicket(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false, defaultValue = "asc") String sort,
            @RequestParam(required = false) String sidoFilter,
            @RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getMyTicket(Long.parseLong(userId), page, size, sort, sidoFilter));
    }

    @PostMapping("/all-ticketing-user")
    public List<UserSimpleDto> getTicketingUsers(@RequestBody List<Long> userIds){
        List<UserSimpleDto> res = userService.getTicketingUsers(userIds);
        return res;
    }
}