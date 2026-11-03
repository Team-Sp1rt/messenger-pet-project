package messenger.backend.messaging;

import messenger.backend.dtos.ChatCreatedEvent;
import messenger.backend.dtos.ChatEventType;
import messenger.backend.dtos.ChatCreated;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.UserSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class WebSocketChatEventListener {
    private static final Logger log =
            LoggerFactory.getLogger(WebSocketChatEventListener.class);

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketChatEventListener(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onChatCreated(ChatCreated event) {
        Chat chat = event.chat();

        ChatCreatedEvent notification =
                new ChatCreatedEvent(ChatEventType.CHAT_CREATED, chat);

        for (UserSummary member : chat.getMembers()) {
            try {
                messagingTemplate.convertAndSendToUser(
                        member.getId().toString(),
                        "/queue/chats",
                        notification
                );
            } catch (MessagingException e) {
                log.error(
                        "Failed to publish CHAT_CREATED: chatId={}, userId={}",
                        chat.getId(),
                        member.getId(),
                        e
                );
            }
        }
    }
}
