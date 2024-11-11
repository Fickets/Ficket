package com.example.ficketuser;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FicketUserApplication {

    public static void main(String[] args) {
        SpringApplication.run(FicketUserApplication.class, args);
    }

}
