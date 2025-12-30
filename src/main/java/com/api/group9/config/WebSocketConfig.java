package com.api.group9.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private AuthChannelInterceptorAdapter authInterceptor;
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // Đây là cái cổng để Android/Web kết nối vào (ws://localhost:8080/ws)
        registry.addEndpoint("/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Prefix cho các tin nhắn gửi từ Client lên Server
        registry.setApplicationDestinationPrefixes("/app");
        
        // Kích hoạt broker để đẩy tin về Client (dùng cho chat 1-1 và public)
        registry.enableSimpleBroker("/user");
        
        // Prefix dành riêng cho user (để gửi tin nhắn riêng tư)
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authInterceptor);
    }
}