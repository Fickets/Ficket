package com.example.ficketadmin.global.config;

import com.example.ficketadmin.domain.admin.service.CustomAdminDetailsService;
import com.example.ficketadmin.global.jwt.JwtAdminFilter;
import com.example.ficketadmin.global.jwt.JwtUtils;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
@AllArgsConstructor
public class SecurityConfig {

    private final CustomAdminDetailsService customAdminDetailsService;
    private final JwtUtils jwtUtils;

    private static final String[] WHITELIST = {
        "/api/v1/admins/login"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        //CSRF, CORS
        http.csrf((csrf) -> csrf.disable());
        http.cors(Customizer.withDefaults());

        // 세션 관리 상태 없음으로 구성
        http.sessionManagement(sessionManagement -> sessionManagement
                .sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
        );

        // FormLogin, httpBasic 비활성
        http.formLogin((form) -> form.disable());
        http.httpBasic(AbstractHttpConfigurer::disable);

        // JWT 필터 UsernamePasswordAuthenticationFilter 앞에 추가
        http.addFilterBefore(new JwtAdminFilter(customAdminDetailsService, jwtUtils), UsernamePasswordAuthenticationFilter.class);

        http.authorizeHttpRequests(authorize -> authorize
                .requestMatchers(WHITELIST).permitAll()
                .anyRequest().permitAll()
        );

        return http.build();
    }
}
