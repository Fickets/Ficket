package com.example.ficketadmin.domain.admin.controller;


import com.example.ficketadmin.domain.admin.dto.common.AdminDto;
import com.example.ficketadmin.domain.admin.dto.request.AdminLoginReq;
import com.example.ficketadmin.domain.admin.dto.response.AdminLoginRes;
import com.example.ficketadmin.domain.admin.service.AdminService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class AdminController {

    private final AdminService adminService;

    /**
     * 관리자 로그인 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-25
     * 변경 이력:
     * - 2024-11-25 최용수: 초기 작성
     */
    @PostMapping("/login")
    public ResponseEntity<AdminDto> login(@RequestBody AdminLoginReq adminRoginReq, HttpServletResponse response){

        AdminDto res = adminService.login(adminRoginReq, response);

        return ResponseEntity.ok(res);
    }

    /**
     * 관리자 로그아웃 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-25
     * 변경 이력:
     * - 2024-11-25 최용수: 초기 작성
     */
    @GetMapping("/logout")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')") // 두 권한중 하나라도 있어야 사용 가능
    public ResponseEntity<Integer> logout(HttpServletRequest request, HttpServletResponse response){

        adminService.logout(request, response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    /**
     * 관리자 Access 재발급 API
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-25
     * 변경 이력:
     * - 2024-11-25 최용수: 초기 작성
     */
    @GetMapping("/reissue")
    @PreAuthorize("hasAnyRole('MANAGER', 'USER')") // 두 권한중 하나라도 있어야 사용 가능
    public ResponseEntity<Integer> reissue(HttpServletRequest request, HttpServletResponse response) {

        // 토큰 재발급
        adminService.reissue(request, response);

        return ResponseEntity.status(HttpStatus.OK).build();

    }


    /**
     * 관리자 상세정보 조회
     * <p>
     * 작업자: 최용수
     * 작업 날짜: 2024-11-28
     * 변경 이력:
     * - 2024-11-28 최용수: 초기 작성
     */
    @GetMapping("/{adminId}")
    public AdminDto getAdmin(@PathVariable Long adminId){

        return adminService.getAdmin(adminId);
    }
    
    
    //TODO   기능                 메소드/이름     path:/api/v1
    //TODO  수동정산                /GET/CYS     /admins/adjustment/{event_id}
    
    //TODO  거래처리스트조회        /GET/OHS        /admins/companies
    //TODO  관리자상세공연조회      /GET/CYS         /admins/events/{eventId}
    
    //TODO  관리자리스트조회        /GET/OHS        /admins/list
    //TODO  고객리스트조회         /GET/CYS         /admins/users
    //TODO  고객상세조회           /GET/CYS         /admins/users/{userId}
    //TODO  고겍예매취소시키기      /DELETE/CYS      /admins/users/{userId}/{ticketId}
    //TODO  고객회원탈퇴시키기      /DELETE/CYS      /admins/users/{userId}
}
