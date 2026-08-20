package messenger.backend.messaging;

import messenger.backend.dtos.ChatEventType;
import messenger.backend.dtos.Message;
import messenger.backend.dtos.MessageChangedEvent;
import messenger.backend.dtos.MessageDeletedEvent;
import messenger.backend.dtos.responses.MessageResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessagePublisher implements MessageEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishCreated(Message message) {
        send(
                message.chatID(),
                new MessageChangedEvent(ChatEventType.MESSAGE_CREATED, toResponse(message))
        );
    }

    @Override
    public void publishUpdated(Message message) {
        send(
                message.chatID(),
                new MessageChangedEvent(ChatEventType.MESSAGE_UPDATED, toResponse(message))
        );
    }

    @Override
    public void publishDeleted(Long chatId, Long messageId) {
        send(
                chatId,
                new MessageDeletedEvent(ChatEventType.MESSAGE_DELETED, chatId, messageId)
        );
    }

    private void send(Long chatId, Object event) {
        messagingTemplate.convertAndSend(
                "/topic/chats/" + chatId + "/events",
                event
        );
    }

    private MessageResponse toResponse(Message message) {
        return new MessageResponse(
                message.id(),
                message.chatID(),
                message.userID(),
                message.content(),
                message.createdAt().toInstant()
        );
    }
}