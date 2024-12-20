package com.example.ficketuser.repository;


import com.example.ficketuser.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialId(Long socialId);
    Optional<User> findByUserId(Long userId);

    @Query(value= "SELECT * FROM user u WHERE u.social_id = :socialId", nativeQuery = true)
    Optional<User> findByDeletedSocialId(@Param("socialId") Long socialId);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user u SET u.deleted_at = null, state=3 WHERE u.user_id = :id", nativeQuery = true)
    void updateUserDeletedAt(@Param("id") Long id);

    @Modifying
    @Transactional
    @Query(value = "UPDATE user SET deleted_at = CURRENT_TIMESTAMP, state = 0 WHERE user_id = :id", nativeQuery = true)
    void customerForceDelete(@Param("id") Long customerId);
}
