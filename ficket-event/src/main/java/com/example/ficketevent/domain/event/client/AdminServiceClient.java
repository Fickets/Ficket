package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.AdminDto;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.Set;

@FeignClient(name = "admin-service", fallback = AdminServiceClientFallbackFactory.class)
public interface AdminServiceClient {

    @GetMapping("/api/v1/admins/{adminId}")
    AdminDto getAdmin(@PathVariable Long adminId);

    @GetMapping("/api/v1/admins/company/{companyId}")
    CompanyResponse getCompany(@PathVariable Long companyId);

    @PostMapping("/api/v1/admins/batch")
    List<AdminDto> getAdminsByIds(@RequestBody Set<Long> adminIds);

    @PostMapping("/api/v1/admins/companies/batch")
    List<CompanyResponse> getCompaniesByIds(@RequestBody Set<Long> companyIds);
}
