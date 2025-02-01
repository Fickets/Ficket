package com.example.ficketqueue.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisWorkConfig {

    @Value("${spring.data.work.host}")
    private String host;

    @Value("${spring.data.work.port}")
    private int port;

    @Bean(name = "workReactiveRedisConnectionFactory")
    public ReactiveRedisConnectionFactory workReactiveRedisConnectionFactory() {
        return new LettuceConnectionFactory(host, port);
    }

    @Bean(name = "workReactiveRedisTemplate")
    public ReactiveRedisTemplate<String, String> workReactiveRedisTemplate(
            @Qualifier("workReactiveRedisConnectionFactory") ReactiveRedisConnectionFactory reactiveRedisConnectionFactory) {

        RedisSerializationContext<String, String> serializationContext =
                RedisSerializationContext.<String, String>newSerializationContext(new StringRedisSerializer())
                        .key(new StringRedisSerializer())
                        .value(new StringRedisSerializer())
                        .hashKey(new StringRedisSerializer())
                        .hashValue(new StringRedisSerializer())
                        .build();

        return new ReactiveRedisTemplate<>(reactiveRedisConnectionFactory, serializationContext);
    }


    @Bean(name = "reactiveRedisMessageListenerContainer")
    public ReactiveRedisMessageListenerContainer reactiveRedisMessageListenerContainer(
            @Qualifier("workReactiveRedisConnectionFactory") ReactiveRedisConnectionFactory reactiveRedisConnectionFactory,
            RedisKeyExpirationListener redisKeyExpirationListener) {
        ReactiveRedisMessageListenerContainer container = new ReactiveRedisMessageListenerContainer(reactiveRedisConnectionFactory);

        // Keyspace Notifications에서 TTL 만료 이벤트 감지
        ChannelTopic expiredTopic = new ChannelTopic("__keyevent@0__:expired");

        container.receive(expiredTopic)
                .flatMap(message -> redisKeyExpirationListener.handleExpireKey(message.getMessage()))
                .subscribe();

        return container;
    }
}