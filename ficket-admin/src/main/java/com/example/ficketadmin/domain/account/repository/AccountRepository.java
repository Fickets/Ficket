package com.example.ficketadmin.domain.account.repository;

import com.example.ficketadmin.domain.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;


public interface AccountRepository extends JpaRepository<Account, Long> {

}
