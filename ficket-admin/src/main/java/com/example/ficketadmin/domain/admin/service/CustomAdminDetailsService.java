package com.example.ficketadmin.domain.admin.service;

import com.example.ficketadmin.domain.admin.dto.common.AdminInfoDto;
import com.example.ficketadmin.domain.admin.dto.common.CustomAdminDetails;
import com.example.ficketadmin.domain.admin.entity.Admin;
import com.example.ficketadmin.domain.admin.mapper.AdminMapper;
import com.example.ficketadmin.domain.admin.repository.AdminRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class CustomAdminDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final AdminMapper adminMapper;

    @Override
    public UserDetails loadUserByUsername(String adminId) throws UsernameNotFoundException {
        Admin admin = adminRepository.findByAdminId(Long.parseLong(adminId))
                .orElseThrow(() -> new UsernameNotFoundException("Not found Admin"));

        AdminInfoDto adminInfoDto = adminMapper.toAdminInfoDto(admin);

        return new CustomAdminDetails(adminInfoDto);
    }
}
