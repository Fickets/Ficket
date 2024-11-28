package com.example.ficketuser.Entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;


@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@RedisHash(value = "userToken")
public class UserTokenRedis {

        @Id
        private Long userId;

        private String refreshToken;

        @TimeToLive
        @Builder.Default
        private Long ttl = 1209600L;
    }

