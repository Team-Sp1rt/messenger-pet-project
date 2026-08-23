package messenger.backend.security;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StompJwtAuthenticationInterceptorTest {

    @Mock
    private JwtDecoder jwtDecoder;

    @Mock
    private MessageChannel channel;

    @InjectMocks
    private StompJwtAuthenticationInterceptor interceptor;

    @Test
    void connectWithValidJwt_setsAuthenticatedUser() {
        Jwt jwt = Jwt.withTokenValue("valid-token")
                .header("alg", "HS256")
                .subject("42")
                .claim("username", "alex")
                .build();

        when(jwtDecoder.decode("valid-token")).thenReturn(jwt);

        Message<byte[]> message = createMessage(StompCommand.CONNECT, "Bearer valid-token");

        Message<?> result = interceptor.preSend(message, channel);

        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(result, StompHeaderAccessor.class);

        assertNotNull(accessor);
        assertInstanceOf(JwtAuthenticationToken.class, accessor.getUser());
        assertEquals("42", accessor.getUser().getName());

        verify(jwtDecoder).decode("valid-token");
    }

    @Test
    void connectWithoutAuthorization_throwsUnauthorized() {
        Message<byte[]> message = createMessage(StompCommand.CONNECT, null);

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );

        assertTrue(exception.getMessage().contains("JWT is missing"));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void connectWithEmptyBearerToken_throwsUnauthorized() {
        Message<byte[]> message = createMessage(StompCommand.CONNECT, "Bearer    ");

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );

        assertTrue(exception.getMessage().contains("JWT is missing"));
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void connectWithInvalidOrExpiredJwt_throwsUnauthorized() {
        when(jwtDecoder.decode("invalid-token")).thenThrow(new JwtException("Invalid token"));

        Message<byte[]> message = createMessage(
                StompCommand.CONNECT,
                "Bearer invalid-token"
        );

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );

        assertTrue(exception.getMessage().contains("JWT is invalid or expired"));

        verify(jwtDecoder).decode("invalid-token");
    }

    @Test
    void connectWithJwtWithoutSubject_throwsUnauthorized() {
        Jwt jwt = Jwt.withTokenValue("token-without-subject")
                .header("alg", "HS256")
                .claim("username", "alex")
                .build();

        when(jwtDecoder.decode("token-without-subject")).thenReturn(jwt);

        Message<byte[]> message = createMessage(
                StompCommand.CONNECT,
                "Bearer token-without-subject"
        );

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, channel)
        );

        assertTrue(exception.getMessage().contains("JWT does not contain user ID"));
    }

    @Test
    void sendFrame_doesNotRequireJwtAgain() {
        Message<byte[]> message = createMessage(StompCommand.SEND, null);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
        verifyNoInteractions(jwtDecoder);
    }

    @Test
    void subscribeFrame_doesNotRequireJwtAgain() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, null);

        Message<?> result = interceptor.preSend(message, channel);

        assertSame(message, result);
        verifyNoInteractions(jwtDecoder);
    }

    private Message<byte[]> createMessage(
            StompCommand command,
            String authorization
    ) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);

        if (authorization != null) {
            accessor.setNativeHeader(HttpHeaders.AUTHORIZATION, authorization);
        }

        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}