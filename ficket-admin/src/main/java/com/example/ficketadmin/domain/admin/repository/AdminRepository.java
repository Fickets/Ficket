package com.example.ficketadmin.domain.admin.repository;

import com.example.ficketadmin.domain.admin.entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AdminRepository extends JpaRepository<Admin, Long> {

    Optional<Admin> findByAdminId(Long adminId);

    Optional<Admin> findById(String id);


}
