package com.example.ficketuser.repository;

import com.example.ficketuser.Entity.UserTokenRedis;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface UserTokenRedisRepository extends CrudRepository<UserTokenRedis, Long> {

    Optional<UserTokenRedis> findByUserId(Long userId);
}
