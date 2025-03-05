package com.example.ficketqueue.global.redis;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisSlotConfig {

    @Value("${spring.data.slot.host}")
    private String host;

    @Value("${spring.data.slot.port}")
    private int port;

    /**
     * Reactive Redis Connection Factory 생성
     */
    @Bean(name = "slotReactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory slotReactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    /**
     * Reactive Redis Template 생성
     */
    @Bean(name = "slotReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, String> slotReactiveRedisTemplate(
            @Qualifier("slotReactiveRedisConnectionFactory") ReactiveRedisConnectionFactory connectionFactory) {

        // Redis Serialization 설정
        RedisSerializationContext<String, String> serializationContext = RedisSerializationContext
                .<String, String>newSerializationContext(new StringRedisSerializer())
                .key(new StringRedisSerializer())
                .value(new StringRedisSerializer())
                .hashKey(new StringRedisSerializer())
                .hashValue(new StringRedisSerializer())
                .build();

        // Reactive Redis Template 생성
        return new ReactiveRedisTemplate<>(connectionFactory, serializationContext);
    }

}
