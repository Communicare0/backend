package com.example.backend.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")            // 클라이언트가 접속할 엔드포인트 (예: ws://.../ws)
                .setAllowedOriginPatterns("*") // CORS 허용
                .withSockJS();                 // SockJS fallback (웹소켓 미지원 브라우저)
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // 클라이언트가 구독할 prefix
        registry.enableSimpleBroker("/topic", "/queue");

        // 클라이언트에서 서버로 보낼 때 붙이는 prefix
        registry.setApplicationDestinationPrefixes("/app");
    }
}