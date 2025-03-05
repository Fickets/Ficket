package com.example.ficketadmin.domain.company.repository;

import com.example.ficketadmin.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Optional<Company> findByCompanyId(Long companyId);

    List<Company> findByCompanyIdIn(Set<Long> companyIds);

}
