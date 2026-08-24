package messenger.backend.services;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.User;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.CreateChatRequest;
import messenger.backend.generated.model.MessagePage;
import messenger.backend.generated.model.UserSummary;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.ChatsRepository;
import messenger.backend.repositories.MessagesRepository;
import messenger.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

    @Mock
    private ChatsRepository chatsRepository;

    @Mock
    private ChatMembersRepository chatMembersRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private MessagesRepository messagesRepository;

    private ChatService chatService;

    @BeforeEach
    void setUp() {
        chatService = new ChatService(chatsRepository, chatMembersRepository, userRepository, messagesRepository);
    }

    @Test
    void createChat_validRequest_returnsCreatedChat() throws SQLException {

        long creatorId = 1L;
        long memberId = 2L;
        long chatId = 10L;

        CreateChatRequest request = new CreateChatRequest(memberId);

        User creator = new User(creatorId, "creator", "", LocalDate.of(2006, 1, 30));
        User member = new User(memberId, "member", "", LocalDate.of(2006, 5, 30));

        when(chatsRepository.insertNewChatReturnsChatID()).thenReturn(chatId);
        when(userRepository.getUserByID(creatorId)).thenReturn(creator);
        when(userRepository.getUserByID(memberId)).thenReturn(member);

        Chat result = chatService.createChat(request, creatorId);

        assertEquals(chatId, result.getId());
        assertEquals(List.of(new UserSummary(creator.id(), creator.username()), new UserSummary(member.id(), member.username())), result.getMembers());
        verify(chatMembersRepository).insertNewChatMember(chatId, creatorId);
        verify(chatMembersRepository).insertNewChatMember(chatId, memberId);

    }

    @Test
    void getChatMessages_moreMessagesThanLimit_returnsPageWithCursor() throws SQLException {
        long chatId = 10L;
        long userId = 1L;
        int limit = 2;

        Message newestMessage = new Message(3L, chatId, userId, "newestMessage", Timestamp.from(Instant.parse("2026-08-24T12:02:00Z")));
        Message middleMessage = new Message(2L, chatId, 2L, "middleMessage", Timestamp.from(Instant.parse("2026-08-24T12:01:00Z")));
        Message oldestMessage = new Message(1L, chatId, userId, "oldestMessage", Timestamp.from(Instant.parse("2026-08-24T12:00:00Z")));

        when(messagesRepository.getLastNMessagesInTheChat(limit + 1, chatId))
                .thenReturn(List.of(newestMessage, middleMessage, oldestMessage));

        MessagePage result = chatService.getChatMessages(chatId, null, limit);

        assertEquals(2, result.getItems().size());
        assertEquals(3L, result.getItems().getFirst().getId());
        assertEquals(2L, result.getItems().getLast().getId());
        assertEquals(2L, result.getNextBeforeMessageId().get());

    }

    @Test
    void getChatMessages_messagesCountEqualsLimit_returnsPageWithNullCursor() throws SQLException {
        long chatId = 10L;
        long userId = 1L;
        int limit = 3;

        Message newestMessage = new Message(3L, chatId, userId, "newestMessage", Timestamp.from(Instant.parse("2026-08-24T12:02:00Z")));

        Message middleMessage = new Message(2L, chatId, 2L, "middleMessage", Timestamp.from(Instant.parse("2026-08-24T12:01:00Z")));

        Message oldestMessage = new Message(1L, chatId, userId, "oldestMessage", Timestamp.from(Instant.parse("2026-08-24T12:00:00Z")));

        when(messagesRepository.getLastNMessagesInTheChat(limit + 1, chatId))
                .thenReturn(List.of(newestMessage, middleMessage, oldestMessage));

        MessagePage result = chatService.getChatMessages(chatId, null, limit);

        assertEquals(3, result.getItems().size());
        assertEquals(3L, result.getItems().getFirst().getId());
        assertEquals(2L, result.getItems().get(1).getId());
        assertEquals(1L, result.getItems().getLast().getId());
        assertNull(result.getNextBeforeMessageId().get());
    }

    @Test
    void getChatMessagesBefore_messagesCountEqualsLimit_returnsPageWithNullCursor() throws SQLException {
        long chatId = 10L;
        long userId = 1L;
        int limit = 2;
        long beforeId = 3L;

        Message newestMessage = new Message(3L, chatId, userId, "newestMessage", Timestamp.from(Instant.parse("2026-08-24T12:02:00Z")));
        Message middleMessage = new Message(2L, chatId, 2L, "middleMessage", Timestamp.from(Instant.parse("2026-08-24T12:01:00Z")));
        Message oldestMessage = new Message(1L, chatId, userId, "oldestMessage", Timestamp.from(Instant.parse("2026-08-24T12:00:00Z")));

        when(messagesRepository.getMessageById(beforeId)).thenReturn(newestMessage);
        when(messagesRepository.getNMessagesInTheChatBeforeMessage(limit + 1, chatId, newestMessage))
                .thenReturn(List.of(middleMessage, oldestMessage));

        MessagePage result = chatService.getChatMessages(chatId, beforeId, limit);

        assertEquals(2, result.getItems().size());
        assertEquals(2L, result.getItems().getFirst().getId());
        assertEquals(1L, result.getItems().getLast().getId());
        assertNull(result.getNextBeforeMessageId().get());
    }
}