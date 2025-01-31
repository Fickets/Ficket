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


//    @Bean(name = "workRedisMessageListenerContainer")
//    public RedisMessageListenerContainer redisMessageListenerContainer(
//            @Qualifier("workRedisConnectionFactory") RedisConnectionFactory connectionFactory,
//            KeyExpirationListener keyExpirationListener) {
//        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
//        container.setConnectionFactory(connectionFactory);
//
//        // Keyspace Notifications에서 TTL 만료 이벤트 감지
//        container.addMessageListener(
//                keyExpirationListener,
//                new PatternTopic("__keyevent@*__:expired")
//        );
//        return container;
//    }
}