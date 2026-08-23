package messenger.backend.security;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessageDeliveryException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class StompDestinationAccessInterceptor implements ChannelInterceptor {

    private static final String APPLICATION_PREFIX = "/app/";
    private static final String PERSONAL_ERRORS_DESTINATION = "/user/queue/errors";
    private static final Pattern CHAT_EVENTS_DESTINATION = Pattern.compile("^/topic/chats/(\\d+)/events$");

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor == null || accessor.getCommand() == null) {
            return message;
        }

        StompCommand command = accessor.getCommand();

        if (command != StompCommand.SEND && command != StompCommand.SUBSCRIBE) {
            return message;
        }

        requireAuthenticatedUser(accessor);

        if (command == StompCommand.SEND) {
            checkSendDestination(accessor);
        } else {
            checkSubscribeDestination(accessor);
        }

        return message;
    }

    private void checkSendDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || destination.isBlank()) {
            throw new MessageDeliveryException("ACCESS_DENIED: Destination is missing");
        }

        if (!destination.startsWith(APPLICATION_PREFIX)) {
            throw new MessageDeliveryException("ACCESS_DENIED: Clients can send only to /app/**");
        }
    }

    private void checkSubscribeDestination(StompHeaderAccessor accessor) {
        String destination = accessor.getDestination();

        if (destination == null || destination.isBlank()) {
            throw new MessageDeliveryException("ACCESS_DENIED: Destination is missing");
        }

        if (PERSONAL_ERRORS_DESTINATION.equals(destination)) {
            return;
        }

        Matcher chatEventsMatcher = CHAT_EVENTS_DESTINATION.matcher(destination);

        if (chatEventsMatcher.matches()) {
            Long chatId = Long.valueOf(chatEventsMatcher.group(1));
            Long userId = Long.valueOf(accessor.getUser().getName());
            // TODO: Проверить членство пользователя через сервис сони, которого еще нет:
            // service.checkUserInChat(userId, chatId);

            return;
        }

        throw new MessageDeliveryException("ACCESS_DENIED: Subscription destination is not allowed");
    }

    private void requireAuthenticatedUser(StompHeaderAccessor accessor) {
        if (accessor.getUser() == null) {
            throw new MessageDeliveryException("ACCESS_DENIED: Authentication is required");
        }

        String userId = accessor.getUser().getName();

        if (userId == null || userId.isBlank()) {
            throw new MessageDeliveryException("ACCESS_DENIED: User ID is missing");
        }
    }
}