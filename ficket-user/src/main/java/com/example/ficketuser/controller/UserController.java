package com.example.ficketuser.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/users")
public class UserController {

    private final Environment env;

    @GetMapping
    public String test() {
        return "user-test";
    }

    @GetMapping("/health_check")
    public String status() {
        return String.format("It's Working in User Service"
                + ", port(local.server.port) = " + env.getProperty("local.server.port")
                + ", port(server.port) = " + env.getProperty("server.port")
                + ", token secret = " + env.getProperty("jwt.secret")
                + ", token expiration time = " + env.getProperty("jwt.access.expiration")
                + ", token refresh time = " + env.getProperty("jwt.refresh.expiration"));
    }
}