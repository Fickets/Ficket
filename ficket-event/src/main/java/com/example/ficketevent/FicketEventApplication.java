package com.example.ficketevent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FicketEventApplication {

	public static void main(String[] args) {
		SpringApplication.run(FicketEventApplication.class, args);
	}

}
