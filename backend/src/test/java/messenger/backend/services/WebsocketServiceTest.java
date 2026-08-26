package messenger.backend.services;

import messenger.backend.dtos.*;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.messaging.WebSocketMessagePublisher;
import messenger.backend.repositories.MessagesRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.sql.SQLException;

@ExtendWith(MockitoExtension.class)
public class WebsocketServiceTest {

    long chatId;
    long userId;
    long messageId;
    String content;

    @Mock
    private ChatMembershipService chatMembershipService;

    @Mock
    private MessagesRepository messagesRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @Mock
    private messenger.backend.dtos.Message message;

    @Mock
    WebSocketMessagePublisher messagePublisher;

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

        when(messagesRepository.insertNewMessageReturnsMessage(any(NewMessage.class))).thenReturn(message);

        websocketService.sendMessage(chatId, userId, content);

        ArgumentCaptor<messenger.backend.dtos.Message> captorMessageEvent =
                ArgumentCaptor.forClass(messenger.backend.dtos.Message.class);
        ArgumentCaptor<NewMessage> captorNewMessage =
                ArgumentCaptor.forClass(NewMessage.class);

        verify(chatMembershipService).checkUserInChat(userId, chatId);

        verify(messagesRepository, times(1))
                .insertNewMessageReturnsMessage(captorNewMessage.capture());

        verify(messagePublisher, times(1))
                .publishCreated(captorMessageEvent.capture());

        NewMessage newMessage = captorNewMessage.getValue();

        assertEquals(chatId, newMessage.chatID());
        assertEquals(userId, newMessage.userID());
        assertEquals(content, newMessage.content());

        messenger.backend.dtos.Message event = captorMessageEvent.getValue();

        assertEquals(message, event);
    }


    @Test
    void failedCheckingUserInChat_AndThrowException() throws SQLException {

        doThrow(new WebsocketServiceException(
                WebSocketErrorCode.CHAT_ACCESS_DENIED,
                "User is not a member of this chat"
        )).when(chatMembershipService).checkUserInChat(userId, chatId);

        WebsocketServiceException exception = assertThrows(
                WebsocketServiceException.class,
                () -> websocketService.sendMessage(chatId, userId, content)
        );

        assertEquals("User is not a member of this chat", exception.getMessage());
        assertEquals(WebSocketErrorCode.CHAT_ACCESS_DENIED, exception.getCode());

        verify(messagesRepository, never())
                .insertNewMessageReturnsMessage(any(NewMessage.class));
        verify(chatMembershipService).checkUserInChat(userId, chatId);
        verify(simpMessagingTemplate, never())
                .convertAndSend(any(), any(MessageChangedEvent.class));

    }

    @Test
    void failedSavingMessageWhenSendMessage_AndThrowException() throws SQLException {

        SQLException sqlException = new SQLException("Couldn't add message due to unknown reason");

        when(messagesRepository.insertNewMessageReturnsMessage(any(NewMessage.class))).thenThrow(sqlException);


        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.sendMessage(chatId, userId, content));

        assertEquals(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, websocketServiceException.getCode());
        assertEquals("Couldn't send message due to unknown reason", websocketServiceException.getMessage());

        verify(chatMembershipService).checkUserInChat(userId, chatId);

        verify(messagesRepository, times(1))
                .insertNewMessageReturnsMessage(any(NewMessage.class));

        verify(messagePublisher, never())
                .publishCreated(any());

    }

    @Test
    void failedPublishMessageWhenSendMessage_AndMakeLog() throws SQLException {

        MessagingException exception = new MessagingException("Failed publish");

        when(messagesRepository.insertNewMessageReturnsMessage(any(NewMessage.class))).thenReturn(message);

        doThrow(exception)
                .when(messagePublisher).publishCreated(message);

        websocketService.sendMessage(chatId, userId, content);

        verify(chatMembershipService).checkUserInChat(userId, chatId);

        verify(messagesRepository, times(1))
                .insertNewMessageReturnsMessage(any(NewMessage.class));

        verify(messagePublisher, times(1))
                .publishCreated(message);

    }

    @Test
    void successfulEditMessage() throws SQLException {

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);
        when(messagesRepository.editMessageReturnsMessage(messageId, content)).thenReturn(message);

        websocketService.editMessage(chatId, userId, messageId, content);

        ArgumentCaptor<messenger.backend.dtos.Message> captor =
                ArgumentCaptor.forClass(messenger.backend.dtos.Message.class);
        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .editMessageReturnsMessage(messageId, content);

        verify(messagePublisher, times(1))
                .publishUpdated(captor.capture());

        messenger.backend.dtos.Message event = captor.getValue();

        assertEquals(message, event);
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

        verify(messagePublisher, never())
                .publishCreated(any());

    }

    @Test
    void failedSavingMessageInDataBaseWhenEditMessage_AndThrowException() throws SQLException {
        SQLException sqlException = new SQLException("Couldn't add message due to unknown reason");

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);
        when(messagesRepository.editMessageReturnsMessage(messageId, content))
                .thenThrow(sqlException);

        WebsocketServiceException websocketServiceException = assertThrows(WebsocketServiceException.class,
                () -> websocketService.editMessage(chatId, userId, messageId, content));

        assertEquals(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, websocketServiceException.getCode());
        assertEquals("Couldn't edit message due to unknown reason", websocketServiceException.getMessage());

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .editMessageReturnsMessage(messageId, content);

        verify(messagePublisher, never())
                .publishCreated(any());

    }

    @Test
    void failedPublishMessageWhenEditMessage_AndMakeLog() throws SQLException {

        MessagingException exception = new MessagingException("Failed publish");

        when(messagesRepository.editMessageReturnsMessage(messageId, content)).thenReturn(message);
        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);

        doThrow(exception)
                .when(messagePublisher).publishUpdated(message);

        websocketService.editMessage(chatId, userId, messageId, content);

        verify(chatMembershipService).checkUserInChat(userId, chatId);

        verify(messagesRepository, times(1))
                .editMessageReturnsMessage(messageId, content);

        verify(messagePublisher, times(1))
                .publishUpdated(message);

    }

    @Test
    void successfulDeleteMessage() throws SQLException {

        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);

        websocketService.deleteMessage(chatId, userId, messageId);

        verify(messagesRepository, times(1))
                .getUserIDByMessageID(messageId);

        verify(messagesRepository, times(1))
                .deleteMessage(messageId);

        verify(messagePublisher, times(1))
                .publishDeleted(chatId, messageId);

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

        verify(messagePublisher, never())
                .publishCreated(any());

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

        verify(messagePublisher, never())
                .publishCreated(any());

    }

    @Test
    void failedPublishMessageWhenDeleteMessage_AndMakeLog() throws SQLException {

        MessagingException exception = new MessagingException("Failed publish");

        doThrow(exception)
                .when(messagePublisher).publishDeleted(chatId, messageId);
        when(messagesRepository.getUserIDByMessageID(messageId)).thenReturn(userId);

        websocketService.deleteMessage(chatId, userId, messageId);

        verify(chatMembershipService).checkUserInChat(userId, chatId);

        verify(messagesRepository, times(1))
                .deleteMessage(messageId);

        verify(messagePublisher, times(1))
                .publishDeleted(chatId, messageId);

    }

}
