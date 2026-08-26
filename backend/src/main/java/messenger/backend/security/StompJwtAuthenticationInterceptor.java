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

import java.security.Principal;
import java.util.List;

@Component
public class StompJwtAuthenticationInterceptor implements ChannelInterceptor {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtDecoder jwtDecoder;

    public StompJwtAuthenticationInterceptor(JwtDecoder jwtDecoder) {
        this.jwtDecoder = jwtDecoder;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command != StompCommand.CONNECT && command != StompCommand.SEND && command != StompCommand.SUBSCRIBE) {
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

            if (command == StompCommand.CONNECT) {
                accessor.setUser(authentication);
                return message;
            }

            Principal connectedUser = accessor.getUser();

            if (connectedUser == null) {
                throw unauthorized("WebSocket session is not authenticated");
            }

            if (!userId.equals(connectedUser.getName())) {
                throw unauthorized("JWT does not belong to this WebSocket session");
            }

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