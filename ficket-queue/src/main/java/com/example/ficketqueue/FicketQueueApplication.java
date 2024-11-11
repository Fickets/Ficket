package com.example.ficketqueue;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FicketQueueApplication {

	public static void main(String[] args) {
		SpringApplication.run(FicketQueueApplication.class, args);
	}

}