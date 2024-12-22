package com.example.ficketuser.global.security;


import com.example.ficketuser.global.jwt.CustomSuccessHandler;
import com.example.ficketuser.global.jwt.JwtFilter;
import com.example.ficketuser.global.jwt.JwtUtils;
import com.example.ficketuser.service.OAuth2UserService;
import lombok.AllArgsConstructor;
import lombok.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@AllArgsConstructor
public class SecurityConfig {

    private final OAuth2UserService oAuth2UserService;
    private final CustomSuccessHandler customSuccessHandler;
    private final JwtUtils jwtUtils;


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

        // JWT 필터 추가
        http
            .addFilterBefore(new JwtFilter(jwtUtils), UsernamePasswordAuthenticationFilter.class);


        // oauth2
        http
                .oauth2Login((oauth2) -> oauth2
                        .userInfoEndpoint((userInfoEndpointConfig -> userInfoEndpointConfig
                                .userService(oAuth2UserService)))
//                        .failureUrl("/events/detail/1")
                        .successHandler(customSuccessHandler));

        http
            .authorizeHttpRequests((auth) -> auth
            .requestMatchers("/oauth2/**","/api/v1/users/**","/user-swagger/v3/api-docs", "/actuator/**").permitAll()
                        .anyRequest().authenticated());

        return http.build();
    }

}
