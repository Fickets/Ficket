package com.example.ficketevent.domain.event.client;

import com.example.ficketevent.domain.event.dto.common.AdminDto;
import com.example.ficketevent.domain.event.dto.common.CompanyResponse;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class AdminServiceClientFallbackFactory implements FallbackFactory<AdminServiceClient> {

    @Override
    public AdminServiceClient create(Throwable cause) {
        return new AdminServiceClient() {

            @Override
            public AdminDto getAdmin(Long adminId) {
                // No Fallback applied
                throw new UnsupportedOperationException("Fallback not implemented for getAdmin");
            }

            @Override
            public CompanyResponse getCompany(Long companyId) {
                // No Fallback applied
                throw new UnsupportedOperationException("Fallback not implemented for getCompany");
            }

            @Override
            public List<AdminDto> getAdminsByIds(Set<Long> adminIds) {
                // Fallback for batch admin retrieval
                return adminIds.stream()
                        .map(adminId -> new AdminDto(adminId, "Unknown Admin", "GUEST"))
                        .collect(Collectors.toList());
            }

            @Override
            public List<CompanyResponse> getCompaniesByIds(Set<Long> companyIds) {
                // Fallback for batch company retrieval
                return companyIds.stream()
                        .map(companyId -> new CompanyResponse(companyId, "Unknown Company"))
                        .collect(Collectors.toList());
            }
        };
    }
}
