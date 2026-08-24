package messenger.backend.config;

import messenger.backend.security.StompDestinationAccessInterceptor;
import messenger.backend.security.StompJwtAuthenticationInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final StompJwtAuthenticationInterceptor authenticationInterceptor;
    private final StompDestinationAccessInterceptor destinationAccessInterceptor;

    public WebSocketConfig(StompJwtAuthenticationInterceptor authenticationInterceptor,
                           StompDestinationAccessInterceptor destinationAccessInterceptor) {
        this.authenticationInterceptor = authenticationInterceptor;
        this.destinationAccessInterceptor = destinationAccessInterceptor;
    }
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws").setAllowedOriginPatterns("http://localhost:*");
    }

    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authenticationInterceptor, destinationAccessInterceptor);
    }
}