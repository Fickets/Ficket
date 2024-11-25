package com.example.ficketevent.global.config.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;


@EnableCaching
@Configuration
@RequiredArgsConstructor
public class RedisCacheConfig {

    private final Environment environment;

    /**
     * Redis Connection Factory 생성
     */
    @Bean
    public RedisConnectionFactory redisCacheConnectionFactory() {
        String redisHost = environment.getProperty("spring.redis.cache.host");
        int redisPort = Integer.parseInt(environment.getProperty("spring.redis.cache.port"));

        return new LettuceConnectionFactory(redisHost, redisPort);
    }


    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisCacheConnectionFactory());
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }


    @Bean(name = "cacheManager") // 캐시에서 Redis를 사용하기 위해 설정
    // RedisCacheManager를 Bean으로 등록하면 기본 CacheManager를 RedisCacheManager로 사용함.
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration configuration = RedisCacheConfiguration.defaultCacheConfig()
                .disableCachingNullValues() // null value 캐시안함
                .entryTtl(Duration.ofDays(1L))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer())) // redis 캐시 키 저장방식을 StringSeriallizer로 지정
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())); // redis 캐시 값 저장방식을 GenericJackson2JsonRedisSerializer로 지정


        return RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(connectionFactory).cacheDefaults(configuration).build();
    }
}
