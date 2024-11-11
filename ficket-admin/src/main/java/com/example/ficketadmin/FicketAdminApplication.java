package com.example.ficketadmin;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class FicketAdminApplication {

    public static void main(String[] args) {
        SpringApplication.run(FicketAdminApplication.class, args);
    }

}