package com.example.ficketuser.domain.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import java.io.Serializable;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "userToken")
public class UserTokenRedis implements Serializable {

    @Id
    private Long userId;

    private String refreshToken;

    @TimeToLive
    @Builder.Default
    private Long ttl = 1209600L;
}

