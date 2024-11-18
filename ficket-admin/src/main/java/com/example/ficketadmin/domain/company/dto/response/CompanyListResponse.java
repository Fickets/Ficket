package com.example.ficketadmin.domain.company.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CompanyListResponse {

    private List<CompanyResponse> companyList;

    public CompanyListResponse(List<CompanyResponse> companyList) {
        this.companyList = companyList;
    }
}
