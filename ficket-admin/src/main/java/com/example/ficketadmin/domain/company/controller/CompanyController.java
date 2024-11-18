package com.example.ficketadmin.domain.company.controller;

import com.example.ficketadmin.domain.company.dto.response.CompanyListResponse;
import com.example.ficketadmin.domain.company.dto.response.CompanyResponse;
import com.example.ficketadmin.domain.company.service.CompanyService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/admins")
public class CompanyController {

    private final CompanyService companyService;

    /**
     * 회사 전체 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-18
     * 변경 이력:
     * - 2024-11-18 오형상: 초기 작성
     */
    @GetMapping("/companies")
    public ResponseEntity<CompanyListResponse> retrieveCompanies() {

        CompanyListResponse response = companyService.getCompanies();

        return ResponseEntity.ok(response);
    }

    /**
     * 회사 단건 조회 API
     * <p>
     * 작업자: 오형상
     * 작업 날짜: 2024-11-18
     * 변경 이력:
     * - 2024-11-18 오형상: 초기 작성
     */
    @GetMapping("/company/{companyId}")
    public ResponseEntity<CompanyResponse> retrieveCompany(@PathVariable Long companyId) {
        CompanyResponse response = companyService.getCompany(companyId);

        return ResponseEntity.ok(response);
    }
}
