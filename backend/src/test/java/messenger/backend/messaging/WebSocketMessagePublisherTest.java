package messenger.backend.messaging;

import messenger.backend.dtos.ChatEventType;
import messenger.backend.dtos.MessageDto;
import messenger.backend.dtos.MessageChangedEvent;
import messenger.backend.dtos.MessageDeletedEvent;
import messenger.backend.generated.model.Message;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;


import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneOffset;

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

        MessageDto message = new MessageDto(
                10L,
                15L,
                7L,
                "Привет",
                Timestamp.from(createdAt)
        );

        Message response = new Message(
                10L,
                15L,
                7L,
                "Привет",
                createdAt.atOffset(ZoneOffset.UTC)
        );

        MessageChangedEvent event = new MessageChangedEvent(ChatEventType.MESSAGE_CREATED, response);

        publisher.publishCreated(message);

        verify(messagingTemplate).convertAndSend(
                "/topic/chats/15/events",
                event
        );
    }

    @Test
    void publishUpdated_sendsUpdatedEventToChatTopic() {
        Instant createdAt = Instant.parse("2026-08-20T12:00:00Z");

        MessageDto message = new MessageDto(
                10L,
                15L,
                7L,
                "Исправленный текст",
                Timestamp.from(createdAt)
        );

        Message response = new Message(
                10L,
                15L,
                7L,
                "Исправленный текст",
                createdAt.atOffset(ZoneOffset.UTC)
        );

        MessageChangedEvent event = new MessageChangedEvent(ChatEventType.MESSAGE_UPDATED, response);

        publisher.publishUpdated(message);

        verify(messagingTemplate).convertAndSend(
                "/topic/chats/15/events",
                event
        );
    }

    @Test
    void publishDeleted_sendsDeletedEventToChatTopic() {
        MessageDeletedEvent event = new MessageDeletedEvent(
                ChatEventType.MESSAGE_DELETED,
                15L,
                10L
        );

        publisher.publishDeleted(15L, 10L);

        verify(messagingTemplate).convertAndSend(
                "/topic/chats/15/events",
                event
        );
    }

}