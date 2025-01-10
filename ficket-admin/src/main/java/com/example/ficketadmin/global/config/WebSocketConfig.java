package com.example.ficketadmin.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/sub"); // 구독
        registry.setApplicationDestinationPrefixes("/pub"); // 메시지 보내기 endpoint 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ticketing-check/ws")
                .setAllowedOriginPatterns("*"); // 허용 URL 패턴
//            .withSockJS(); // SockJS 지원을 활성화합니다.
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {

        registration.interceptors(new ChannelInterceptor() {
            @Override
            public Message<?> preSend(Message<?> message, MessageChannel channel) {

                StompHeaderAccessor accessor =
                        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

                switch (accessor.getCommand()) {
                    case CONNECT:
                        // 헤더에서 멤버 아이디 뽑기
                        log.info("CONNECT SUCCESS");
                        break;

                    case SUBSCRIBE:
                        // Destination - sub 주소 저장
                        log.info("SUBSCRIBE SUCCESS");
                        break;

                    case DISCONNECT:
                        // 접속 유저 삭제
                        log.info("DISCONNECT SUCCESS");
                        break;
                }

                return message;
            }
        });
    }
}
