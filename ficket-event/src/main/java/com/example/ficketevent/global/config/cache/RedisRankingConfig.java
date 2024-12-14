package com.example.ficketevent.global.config.cache;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisRankingConfig {

    @Value("${spring.redis.ranking.host}")
    private String host;

    @Value("${spring.redis.ranking.port}")
    private int port;

    /**
     * Ranking Redis Connection Factory 생성
     */
    @Bean(name = "rankingRedisConnectionFactory")
    public RedisConnectionFactory rankingRedisConnectionFactory() {
        RedisStandaloneConfiguration configuration = new RedisStandaloneConfiguration();
        configuration.setHostName(host);
        configuration.setPort(port);
        return new LettuceConnectionFactory(configuration);
    }

    /**
     * Ranking RedisTemplate 생성
     */
    @Bean(name = "rankingRedisTemplate")
    public RedisTemplate<String, Object> rankingRedisTemplate(
            @Qualifier("rankingRedisConnectionFactory") RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(connectionFactory);

        // Key와 Value 직렬화 설정
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new StringRedisSerializer());
        return redisTemplate;
    }
}
