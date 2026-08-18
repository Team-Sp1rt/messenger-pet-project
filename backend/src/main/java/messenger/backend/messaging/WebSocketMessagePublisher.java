package messenger.backend.messaging;

import messenger.backend.dtos.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class WebSocketMessagePublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WebSocketMessagePublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishToChat(Message message) {
        messagingTemplate.convertAndSend(
                "/topic/chats/" + message.chatID() + "/messages",
                message);
    }
}
