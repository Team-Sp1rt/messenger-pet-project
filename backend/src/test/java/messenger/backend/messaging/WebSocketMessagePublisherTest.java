package messenger.backend.messaging;

import messenger.backend.dtos.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.Timestamp;
import java.time.Instant;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WebSocketMessagePublisherTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private WebSocketMessagePublisher publisher;

    @Test
    void publishToChat_sendsMessageToCorrectChatTopic() {
        Message message = new Message(
                10L,
                15L,
                7L,
                "Привет",
                Timestamp.from(Instant.now())
        );

        publisher.publishToChat(message);

        verify(messagingTemplate).convertAndSend(
                "/topic/chats/15/messages",
                message
        );
    }
}