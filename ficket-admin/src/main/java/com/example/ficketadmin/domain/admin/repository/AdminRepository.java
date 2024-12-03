package com.example.ficketadmin.domain.admin.repository;

import com.example.ficketadmin.domain.admin.dto.common.AdminDto;
import com.example.ficketadmin.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByAdminId(Long adminId);

    Optional<Admin> findById(String id);

    List<Admin> findByAdminIdIn(Set<Long> adminIds);
}
