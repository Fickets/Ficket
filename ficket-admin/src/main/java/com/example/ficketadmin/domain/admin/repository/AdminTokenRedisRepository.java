package com.example.ficketadmin.domain.admin.repository;

import com.example.ficketadmin.domain.admin.entity.AdminTokenRedis;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;


@EnableRedisRepositories
public interface AdminTokenRedisRepository extends CrudRepository<AdminTokenRedis, Long> {

    Optional<AdminTokenRedis> findByAdminId(Long adminId);
}
