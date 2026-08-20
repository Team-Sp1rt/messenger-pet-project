package messenger.backend.messaging;

import messenger.backend.dtos.ChatEventType;
import messenger.backend.dtos.Message;
import messenger.backend.dtos.MessageChangedEvent;
import messenger.backend.dtos.responses.MessageResponse;
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
    void publishCreated_sendsCreatedEventToChatTopic() {
        Instant createdAt = Instant.parse("2026-08-20T12:00:00Z");

        Message message = new Message(
                10L,
                15L,
                7L,
                "Привет",
                Timestamp.from(createdAt)
        );

        MessageResponse response = new MessageResponse(
                10L,
                15L,
                7L,
                "Привет",
                createdAt
        );

        MessageChangedEvent event = new MessageChangedEvent(
                ChatEventType.MESSAGE_CREATED,
                response
        );

        publisher.publishCreated(message);

        verify(messagingTemplate).convertAndSend(
                "/topic/chats/15/events",
                event
        );
    }
}