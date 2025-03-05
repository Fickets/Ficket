package com.example.ficketqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.boot.autoconfigure.data.redis.RedisReactiveAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication(exclude = {
		RedisAutoConfiguration.class,
		RedisReactiveAutoConfiguration.class
})
public class FicketQueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(FicketQueueApplication.class, args);
	}

}
