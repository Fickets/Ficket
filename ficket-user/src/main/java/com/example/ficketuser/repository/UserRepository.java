package com.example.ficketuser.repository;


import com.example.ficketuser.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(Long socialId);
    Optional<User> findByUserId(Long userId);
}
