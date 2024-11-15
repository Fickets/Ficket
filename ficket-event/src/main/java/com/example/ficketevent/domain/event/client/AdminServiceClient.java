package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.AdminDto;
import com.example.ficketevent.domain.event.dto.common.CompanyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "admin-service")
public interface AdminServiceClient {

    @GetMapping("/api/v1/admins/{adminId}")
    AdminDto getAdmin(@PathVariable Long adminId);

    @GetMapping("/api/v1/admins/company/{companyId}")
    CompanyDto getCompany(@PathVariable Long companyId);
}
