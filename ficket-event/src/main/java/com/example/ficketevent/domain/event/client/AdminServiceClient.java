package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.AdminDto;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import org.apache.kafka.shaded.io.opentelemetry.proto.trace.v1.Status;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/api/v1/settlements/create-total")
    int createTotalSettlement(@RequestParam Long eventId);
}
