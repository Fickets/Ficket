package com.example.ficketadmin.domain.account.repository;

import com.example.ficketadmin.domain.account.entity.Account;
import com.example.ficketadmin.domain.company.entity.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByCompany(Company company);

}
