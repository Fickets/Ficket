package com.example.ficketticketing;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FicketTicketingApplication {

	public static void main(String[] args) {
		SpringApplication.run(FicketTicketingApplication.class, args);
	}

}
