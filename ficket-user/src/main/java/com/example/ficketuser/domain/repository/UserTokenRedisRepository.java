package com.example.ficketuser.domain.repository;

import com.example.ficketuser.domain.Entity.UserTokenRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserTokenRedisRepository extends CrudRepository<UserTokenRedis, Long> {

    Optional<UserTokenRedis> findByUserId(Long userId);
}
