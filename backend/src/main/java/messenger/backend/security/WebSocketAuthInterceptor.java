package messenger.backend.security;

import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class WebSocketAuthInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    public WebSocketAuthInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() != StompCommand.CONNECT) {
            return message;
        }

        String authorization = accessor.getFirstNativeHeader(HttpHeaders.AUTHORIZATION);

        if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
            throw unauthorized("JWT is missing");
        }

        String token = authorization.substring(BEARER_PREFIX.length()).trim();

        if (token.isEmpty()) {
            throw unauthorized("JWT is missing");
        }

        try {
            Jwt jwt = jwtDecoder.decode(token);

            String userId = jwt.getSubject();

            if (userId == null || userId.isBlank()) {
                throw unauthorized("JWT does not contain user ID");
            }

            JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, List.of(), userId);

            accessor.setUser(authentication);

            return message;
        } catch (JwtException exception) {
            throw unauthorized("JWT is invalid or expired");
        }
    }

    private MessageDeliveryException unauthorized(String reason) {
        return new MessageDeliveryException(
                "UNAUTHORIZED: " + reason
        );
    }
}