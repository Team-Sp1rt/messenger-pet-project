package messenger.backend.security;

import org.junit.jupiter.api.Test;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.MessageBuilder;

import static org.junit.jupiter.api.Assertions.*;

class StompDestinationAccessInterceptorTest {

    private final StompDestinationAccessInterceptor interceptor =
            new StompDestinationAccessInterceptor();

    @Test
    void authenticatedSendToApplicationDestination_isAllowed() {
        Message<byte[]> message = createMessage(StompCommand.SEND, "/app/messages", "42");

        Message<?> result = interceptor.preSend(message, null);

        assertSame(message, result);
    }

    @Test
    void sendToBrokerDestination_isDenied() {
        Message<byte[]> message = createMessage(StompCommand.SEND, "/topic/chats/42/events", "42");

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, null));

        assertTrue(exception.getMessage().contains("Clients can send only to /app/**"));
    }

    @Test
    void unauthenticatedSend_isDenied() {
        Message<byte[]> message = createMessage(StompCommand.SEND, "/app/messages", null);

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, null)
        );

        assertTrue(exception.getMessage().contains("Authentication is required"));
    }

    @Test
    void subscribeToPersonalErrors_isAllowed() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, "/user/queue/errors", "42");

        Message<?> result = interceptor.preSend(message, null);

        assertSame(message, result);
    }

    @Test
    void subscribeToChatEvents_isAllowed() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, "/topic/chats/42/events", "7");

        Message<?> result = interceptor.preSend(message, null);

        assertSame(message, result);
    }

    @Test
    void subscribeToUnsupportedDestination_isDenied() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, "/topic/all-messages", "42");

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, null)
        );

        assertTrue(exception.getMessage().contains("Subscription destination is not allowed"));
    }

    @Test
    void subscribeToChatEventsWithNonNumericChatId_isDenied() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, "/topic/chats/abc/events", "42");

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, null)
        );

        assertTrue(exception.getMessage().contains("Subscription destination is not allowed"));
    }

    @Test
    void unauthenticatedSubscribe_isDenied() {
        Message<byte[]> message = createMessage(StompCommand.SUBSCRIBE, "/topic/chats/42/events", null);

        MessageDeliveryException exception = assertThrows(
                MessageDeliveryException.class,
                () -> interceptor.preSend(message, null)
        );

        assertTrue(exception.getMessage().contains("Authentication is required"));
    }

    private Message<byte[]> createMessage(StompCommand command, String destination, String userId) {
        StompHeaderAccessor accessor = StompHeaderAccessor.create(command);

        if (destination != null) {
            accessor.setDestination(destination);
        }

        if (userId != null) {
            accessor.setUser(() -> userId);
        }

        accessor.setLeaveMutable(true);

        return MessageBuilder.createMessage(new byte[0], accessor.getMessageHeaders());
    }
}