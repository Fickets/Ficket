package com.example.ficketqueue.global.webfluxWebsocket;

import com.example.ficketqueue.global.webfluxWebsocket.handler.QueueStatusWebSocketHandler;
import com.example.ficketqueue.global.webfluxWebsocket.handler.WorkWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.reactive.CorsConfigurationSource;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.WebSocketHandler;


import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final WorkWebSocketHandler workWebSocketHandler;
        private final QueueStatusWebSocketHandler queueStatusWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> handlerMap = new HashMap<>();
        handlerMap.put("/work-status/*", workWebSocketHandler);
        handlerMap.put("/queue-status/*", queueStatusWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(handlerMap);
        mapping.setOrder(-1); // WebSocket 핸들러의 우선 순위를 설정
        mapping.setCorsConfigurationSource(corsConfigurationSource()); // CORS 설정 추가
        return mapping;
    }


    private CorsConfigurationSource corsConfigurationSource() {
        return exchange -> {
            String path = exchange.getRequest().getURI().getPath(); // 요청 URI 경로 가져오기
            if (path.startsWith("/work-status") || path.startsWith("/queue-status")) {
                CorsConfiguration corsConfig = new CorsConfiguration();
                corsConfig.addAllowedOrigin("http://localhost:5173");
                corsConfig.addAllowedOrigin("http://localhost:8089");
                corsConfig.addAllowedMethod("GET");
                corsConfig.addAllowedMethod("POST");
                corsConfig.addAllowedMethod("PUT");
                corsConfig.addAllowedMethod("DELETE");
                corsConfig.addAllowedMethod("OPTIONS");
                corsConfig.addAllowedMethod("HEAD");
                corsConfig.addAllowedHeader("*");
                corsConfig.setAllowCredentials(true);
                return corsConfig;
            }
            return null;
        };
    }
}
