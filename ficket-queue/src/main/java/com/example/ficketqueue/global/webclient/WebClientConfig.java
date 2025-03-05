package com.example.ficketqueue.global.webclient;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class WebClientConfig {

    private final DiscoveryClient discoveryClient;

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.build();
    }

    /**
     * Eureka에서 event-service의 실제 URL을 가져오는 메서드
     */
    public String getEventServiceUrl() {
        return discoveryClient.getInstances("event-service") // 서비스 이름을 기반으로 Eureka에서 검색
                .stream()
                .findFirst()
                .map(serviceInstance -> serviceInstance.getUri().toString())
                .orElseThrow(() -> new RuntimeException("event-service를 Eureka에서 찾을 수 없습니다."));
    }
}
