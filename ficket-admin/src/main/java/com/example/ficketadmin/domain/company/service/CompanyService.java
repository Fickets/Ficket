package com.example.ficketadmin.domain.company.service;

import com.example.ficketadmin.domain.company.dto.response.CompanyListResponse;
import com.example.ficketadmin.domain.company.dto.response.CompanyResponse;
import com.example.ficketadmin.domain.company.entity.Company;
import com.example.ficketadmin.domain.company.repository.CompanyRepository;
import com.example.ficketadmin.domain.mapper.CompanyMapper;
import com.example.ficketadmin.global.result.error.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.example.ficketadmin.global.result.error.ErrorCode.*;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private final CompanyRepository companyRepository;

    private final CompanyMapper companyMapper;

    /**
     * 모든 회사 정보를 조회하고, 결과를 캐싱합니다.
     *
     * @return 회사 목록을 포함한 CompanyListResponse 객체
     */
    @Cacheable(
            cacheNames = "companies",
            key = "'all'",
            cacheManager = "cacheManager",
            unless = "#result == null"
    )
    public CompanyListResponse getCompanies() {
        List<CompanyResponse> companyResponses = mapEventStagesToDto();
        return new CompanyListResponse(companyResponses);
    }

    public CompanyResponse getCompany(Long companyId) {
        Company company = companyRepository.findById(companyId).orElseThrow(() -> new BusinessException(COMPANY_NOT_FOUND));
        return companyMapper.toCompanyResponse(company);
    }

    /**
     * 모든 회사 데이터를 조회하여 DTO로 변환합니다.
     *
     * @return 회사 정보 리스트(CompanyResponse DTO 리스트)
     */
    private List<CompanyResponse> mapEventStagesToDto() {
        return companyRepository.findAll()
                .stream()
                .map(companyMapper::toCompanyResponse) // Entity를 DTO로 변환
                .toList();
    }

}
