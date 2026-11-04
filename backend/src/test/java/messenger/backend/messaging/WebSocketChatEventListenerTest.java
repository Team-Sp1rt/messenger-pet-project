package messenger.backend.messaging;

import messenger.backend.dtos.ChatCreated;
import messenger.backend.dtos.ChatCreatedEvent;
import messenger.backend.dtos.ChatEventType;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.UserSummary;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class WebSocketChatEventListenerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketChatEventListener listener;

    @Test
    void onChatCreated_sendsCreatedEventToBothMembers() {
        Chat chat = new Chat(
                10L,
                List.of(
                        new UserSummary(1L, "creator"),
                        new UserSummary(2L, "member")
                )
        );

        ChatCreatedEvent notification = new ChatCreatedEvent(ChatEventType.CHAT_CREATED, chat);

        listener.onChatCreated(new ChatCreated(chat));

        verify(messagingTemplate).convertAndSendToUser(
                "1",
                "/queue/chats",
                notification
        );
        verify(messagingTemplate).convertAndSendToUser(
                "2",
                "/queue/chats",
                notification
        );
        verifyNoMoreInteractions(messagingTemplate);
    }


}