package com.example.ficketqueue.global.webfluxWebsocket;

import com.example.ficketqueue.global.webfluxWebsocket.handler.QueueStatusWebSocketHandler;
import com.example.ficketqueue.global.webfluxWebsocket.handler.WorkWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.HandlerMapping;
import org.springframework.web.reactive.handler.SimpleUrlHandlerMapping;
import org.springframework.web.reactive.socket.server.WebSocketService;
import org.springframework.web.reactive.socket.server.support.HandshakeWebSocketService;
import org.springframework.web.reactive.socket.server.support.WebSocketHandlerAdapter;

import java.util.HashMap;
import java.util.Map;

@Configuration
@RequiredArgsConstructor
public class WebSocketConfig {

    private final WorkWebSocketHandler workWebSocketHandler;
    private final QueueStatusWebSocketHandler queueStatusWebSocketHandler;

    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, Object> handlerMap = new HashMap<>();

        // 동적 Path 지원
        handlerMap.put("/work-status/{eventId}/{eventScheduleId}", workWebSocketHandler);
        handlerMap.put("/queue-status/{eventId}", queueStatusWebSocketHandler);

        SimpleUrlHandlerMapping mapping = new SimpleUrlHandlerMapping();
        mapping.setUrlMap(handlerMap);
        mapping.setOrder(-1); // WebSocket 핸들러 우선순위 설정
        return mapping;
    }

    @Bean
    public WebSocketHandlerAdapter webSocketHandlerAdapter() {
        return new WebSocketHandlerAdapter(webSocketService());
    }

    @Bean
    public WebSocketService webSocketService() {
        return new HandshakeWebSocketService();
    }
}
