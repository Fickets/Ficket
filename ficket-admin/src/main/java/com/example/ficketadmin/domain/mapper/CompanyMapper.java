package com.example.ficketadmin.domain.mapper;

import com.example.ficketadmin.domain.company.dto.response.CompanyResponse;
import com.example.ficketadmin.domain.company.entity.Company;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CompanyMapper {

    CompanyResponse toCompanyResponse(Company company);
}
