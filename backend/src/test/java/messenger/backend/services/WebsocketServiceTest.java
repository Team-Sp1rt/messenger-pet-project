package messenger.backend.services;

import messenger.backend.dtos.*;
import messenger.backend.exceptions.repostitories.messages.NoSuchMessageException;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.MessagesRepository;
import messenger.backend.generated.model.Message;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class WebsocketServiceTest {

    long chatId;
    long userId;
    long messageId;
    String content;

    @Mock
    private ChatMembersRepository chatMembersRepository;

    @Mock
    private MessagesRepository messagesRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private messenger.backend.dtos.Message message;

    @InjectMocks
    WebsocketService websocketService;

    @BeforeEach
    void setUp() {
        chatId = 12345;
        userId = 654321;
        messageId = 99999;
        content = "Successful test!";
    }

    @Test
    void successfulSaveAndSendMessage() throws SQLException {
        Set<Long> testSet = Set.of(userId);
        Timestamp timestamp = Timestamp.valueOf("2026-08-23 17:30:00");

        when(chatMembersRepository.getAllMembersOfTheChat(chatId)).thenReturn(testSet);
        when(messagesRepository.insertNewMessageReturnsMessage(any(NewMessage.class))).thenReturn(message);
        when(message.id()).thenReturn(messageId);
        when(message.chatID()).thenReturn(chatId);
        when(message.userID()).thenReturn(userId);
        when(message.content()).thenReturn(content);
        when(message.createdAt()).thenReturn(timestamp);

        websocketService.sendMessage(chatId, userId, content);

        ArgumentCaptor<MessageChangedEvent> captorMessageEvent =
                ArgumentCaptor.forClass(MessageChangedEvent.class);
        ArgumentCaptor<NewMessage> captorNewMessage =
                ArgumentCaptor.forClass(NewMessage.class);
        verify(chatMembersRepository, times(1))
                .getAllMembersOfTheChat(chatId);

        verify(messagesRepository, times(1))
                .insertNewMessageReturnsMessage(captorNewMessage.capture());

        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/chats/" + chatId + "/events"), captorMessageEvent.capture());

        NewMessage newMessage = captorNewMessage.getValue();

        assertEquals(chatId, newMessage.chatID());
        assertEquals(userId, newMessage.userID());
        assertEquals(content, newMessage.content());

        MessageChangedEvent event = captorMessageEvent.getValue();
        Message messageResponse = event.message();

        assertEquals(ChatEventType.MESSAGE_CREATED, event.type());
        assertEquals(messageId, messageResponse.getId());
        assertEquals(chatId, messageResponse.getChatId());
        assertEquals(userId, messageResponse.getUserId());
        assertEquals(content, messageResponse.getContent());
        assertEquals(timestamp.toInstant().atOffset(ZoneOffset.UTC), messageResponse.getCreatedAt());
    }


    @Test
    void failedCheckingUserInChat_AndThrowException() throws SQLException {
        long anotherId = 8888;
        Set<Long> testSet = Set.of(anotherId);

        when(chatMembersRepository.getAllMembersOfTheChat(chatId)).thenReturn(testSet);

        WebsocketServiceException exception = assertThrows(
                WebsocketServiceException.class,
                () -> websocketService.sendMessage(chatId, userId, content)
        );

        assertEquals("User is not a member of this chat", exception.getMessage());
        assertEquals(WebSocketErrorCode.CHAT_ACCESS_DENIED, exception.getCode());

        verify(chatMembersRepository, times(1))
                .getAllMembersOfTheChat(chatId);

        verify(messagesRepository, never())
                .insertNewMessageReturnsMessage(any(NewMessage.class));

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void failedSavingMessageWhenSendMessage_AndThrowException() throws SQLException {
        Set<Long> testSet = Set.of(userId);
        SQLException sqlException = new SQLException("Couldn't add message due to unknown reason");

        when(chatMembersRepository.getAllMembersOfTheChat(chatId)).thenReturn(testSet);
        when(messagesRepository.insertNewMessageReturnsMessage(any(NewMessage.class))).thenThrow(sqlException);


        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.sendMessage(chatId, userId, content));

        assertEquals(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, websocketServiceException.getCode());
        assertEquals("Couldn't add message due to unknown reason", websocketServiceException.getMessage());

        verify(chatMembersRepository, times(1))
                .getAllMembersOfTheChat(chatId);

        verify(messagesRepository, times(1))
                .insertNewMessageReturnsMessage(any(NewMessage.class));

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void successfulEditMessage() throws SQLException {
        Timestamp timestamp = Timestamp.valueOf("2026-08-23 17:30:00");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);
        when(messagesRepository.editMessageReturnsMessage(messageId, content)).thenReturn(message);
        when(message.id()).thenReturn(messageId);
        when(message.chatID()).thenReturn(chatId);
        when(message.content()).thenReturn(content);
        when(message.userID()).thenReturn(userId);
        when(message.createdAt()).thenReturn(timestamp);

        websocketService.editMessage(chatId, userId, messageId, content);

        ArgumentCaptor<MessageChangedEvent> captor =
                ArgumentCaptor.forClass(MessageChangedEvent.class);
        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .editMessageReturnsMessage(messageId, content);

        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/chats/" + chatId + "/events"), captor.capture());

        MessageChangedEvent event = captor.getValue();
        Message messageResponse = event.message();

        assertEquals(ChatEventType.MESSAGE_UPDATED, event.type());
        assertEquals(messageId, messageResponse.getId());
        assertEquals(chatId, messageResponse.getChatId());
        assertEquals(userId, messageResponse.getUserId());
        assertEquals(content, messageResponse.getContent());
        assertEquals(timestamp.toInstant().atOffset(ZoneOffset.UTC), messageResponse.getCreatedAt());
    }


    @Test
    void failedCheckingUserRightsWhenEditMessage_AndThrowException() throws SQLException {
        long anotherId = 8888;

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(anotherId);

        WebsocketServiceException exception = assertThrows(
                WebsocketServiceException.class,
                () -> websocketService.editMessage(chatId, userId, messageId, content)
        );

        assertEquals("You can't edit this message", exception.getMessage());
        assertEquals(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, exception.getCode());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, never())
                .editMessageReturnsMessage(any(), any());

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void failedSavingMessageInDataBaseWhenEditMessage_AndThrowException() throws SQLException {
        SQLException sqlException = new SQLException("Couldn't add message due to unknown reason");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);
        when(messagesRepository.editMessageReturnsMessage(messageId, content))
                .thenThrow(sqlException);

        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.editMessage(chatId, userId, messageId, content));

        assertEquals(WebSocketErrorCode.MESSAGE_NOT_FOUND, websocketServiceException.getCode());
        assertEquals("We can't found your message for edit", websocketServiceException.getMessage());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .editMessageReturnsMessage(messageId, content);

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void failedCheckingMessageInChatWhenEditMessage_AndThrowException() throws SQLException {
        NoSuchMessageException messageException = new NoSuchMessageException("Couldn't find message with specified id");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenThrow(messageException);

        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.editMessage(chatId, userId, messageId, content));

        assertEquals(WebSocketErrorCode.MESSAGE_NOT_FOUND, websocketServiceException.getCode());
        assertEquals("We can't found your message for edit", websocketServiceException.getMessage());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, never())
                .editMessageReturnsMessage(messageId, content);

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void successfulDeleteMessage() throws SQLException {

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);

        websocketService.deleteMessage(chatId, userId, messageId);

        ArgumentCaptor<MessageDeletedEvent> captor =
                ArgumentCaptor.forClass(MessageDeletedEvent.class);
        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .deleteMessage(messageId);

        verify(simpMessagingTemplate, times(1))
                .convertAndSend(eq("/topic/chats/" + chatId + "/events"), captor.capture());

        MessageDeletedEvent event = captor.getValue();

        assertEquals(ChatEventType.MESSAGE_DELETED, event.type());
        assertEquals(messageId, event.messageId());
        assertEquals(chatId, event.chatId());

    }


    @Test
    void failedCheckingUserRightsWhenDeleteMessage_AndThrowException() throws SQLException {
        long anotherId = 8888;

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(anotherId);

        WebsocketServiceException exception = assertThrows(
                WebsocketServiceException.class,
                () -> websocketService.deleteMessage(chatId, userId, messageId)
        );

        assertEquals("You can't delete this message", exception.getMessage());
        assertEquals(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, exception.getCode());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, never())
                .deleteMessage(any());

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageDeletedEvent.class));

    }

    @Test
    void failedDeleteMessageInDataBaseWhenDeleteMessage_AndThrowException() throws SQLException {
        SQLException sqlException = new SQLException("Couldn't delete message due to unknown reason");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);
        doThrow(sqlException)
                .when(messagesRepository)
                .deleteMessage(messageId);

        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.deleteMessage(chatId, userId, messageId));

        assertEquals(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, websocketServiceException.getCode());
        assertEquals("Couldn't delete message due to unknown reason", websocketServiceException.getMessage());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .deleteMessage(messageId);

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageDeletedEvent.class));

    }

    @Test
    void failedCheckingMessageInChatWhenDeleteMessage_AndThrowException() throws SQLException {
        NoSuchMessageException messageException = new NoSuchMessageException("Couldn't find message with specified id");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenThrow(messageException);

        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.deleteMessage(chatId, userId, messageId));

        assertEquals(WebSocketErrorCode.MESSAGE_NOT_FOUND, websocketServiceException.getCode());
        assertEquals("Couldn't find message with specified id", websocketServiceException.getMessage());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, never())
                .editMessageReturnsMessage(messageId, content);

        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }
}
