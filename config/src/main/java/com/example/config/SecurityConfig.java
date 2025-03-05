package com.example.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/encrypt", "/decrypt").authenticated() // 인증 필요
                        .anyRequest().permitAll() // 나머지 요청은 허용
                )
                .httpBasic(httpBasic -> httpBasic.realmName("Ficket")); // HTTP Basic 인증 설정
        return http.build();
    }

}
