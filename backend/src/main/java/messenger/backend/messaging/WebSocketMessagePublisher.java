package messenger.backend.messaging;

import messenger.backend.dtos.ChatEventType;
import messenger.backend.dtos.MessageDto;
import messenger.backend.dtos.MessageChangedEvent;
import messenger.backend.dtos.MessageDeletedEvent;
import messenger.backend.generated.model.Message;
import messenger.backend.mappers.MessageMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessagePublisher implements MessageEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @Override
    public void publishCreated(MessageDto message) {
        send(
                message.chatID(),
                new MessageChangedEvent(ChatEventType.MESSAGE_CREATED, MessageMapper.toApi(message))
        );
    }

    @Override
    public void publishUpdated(MessageDto message) {
        send(
                message.chatID(),
                new MessageChangedEvent(ChatEventType.MESSAGE_UPDATED, MessageMapper.toApi(message))
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
}