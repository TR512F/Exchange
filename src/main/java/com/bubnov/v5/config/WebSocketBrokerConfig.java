package com.bubnov.v5.config;

import com.bubnov.v5.service.JwtService;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketBrokerConfig implements WebSocketMessageBrokerConfigurer {


//    private final JwtService jwtService;
//    private final JwtHandshakeInterceptor interceptor;
//
//    public WebSocketBrokerConfig(JwtService jwtService, JwtHandshakeInterceptor interceptor) {
////        this.jwtService = jwtService;
//        this.interceptor = interceptor;
//    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic","/queue");
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
//                .setAllowedOrigins("*")
//                .addInterceptors(interceptor)
                .withSockJS();
    }
}

//    @Bean
//    public DefaultHandshakeHandler handshakeHandler() {
//        return new DefaultHandshakeHandler() {
//            @Override
//            protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler,
//                                              Map<String, Object> attributes) {
//                String username = (String) attributes.get("username");
//                System.out.println("\n!! determineUser: " + username);
//                return null;
////                return username != null ? new UsernamePrincipal(username) : null;
//            }
//        };
//    }
