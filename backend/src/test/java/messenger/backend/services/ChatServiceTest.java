package messenger.backend.services;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.ReposException;
import messenger.backend.exceptions.repostitories.NoSuchMessageException;
import messenger.backend.exceptions.repostitories.NoSuchUserException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.ChatListResponse;
import messenger.backend.generated.model.ChatSummary;
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
import java.util.Set;

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

    //TODO: тесты на кидание DatabaseException,
    // который кидается после NoSuchUserException и/или NoSuchMessageException
    // для 3 методов класса

    @Test
    void createChat_validRequest_returnsCreatedChat() throws SQLException, ReposException {

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
        List<UserSummary> expected = List.of(new UserSummary(creator.id(), creator.username()), new UserSummary(member.id(), member.username()));
        assertEquals(expected, result.getMembers());
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
    void getChatMessagesBefore_messagesCountEqualsLimit_returnsPageWithNullCursor() throws SQLException, NoSuchMessageException {
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

    @Test
    void getChats_userHasChats_returnsCorrectSummaries() throws SQLException, NoSuchUserException {
        long userId = 1L;
        long firstMemberId = 2L;
        long secondMemberId = 3L;
        long firstChatId = 10L;
        long secondChatId = 20L;

        User user = new User(userId, "current-user", "", LocalDate.of(2000, 1, 1));
        User firstMember = new User(firstMemberId, "first-member", "", LocalDate.of(2001, 2, 2));
        User secondMember = new User(secondMemberId, "second-member", "", LocalDate.of(2002, 3, 3));
        Message lastMessage = new Message(100L, firstChatId, firstMemberId, "last message", Timestamp.from(Instant.parse("2026-08-24T12:00:00Z")));

        when(chatMembersRepository.getAllChatsOfTheMember(userId)).thenReturn(Set.of(firstChatId, secondChatId));
        when(chatMembersRepository.getAllMembersOfTheChat(firstChatId)).thenReturn(Set.of(userId, firstMemberId));
        when(chatMembersRepository.getAllMembersOfTheChat(secondChatId)).thenReturn(Set.of(userId, secondMemberId));
        when(userRepository.getUserByID(userId)).thenReturn(user);
        when(userRepository.getUserByID(firstMemberId)).thenReturn(firstMember);
        when(userRepository.getUserByID(secondMemberId)).thenReturn(secondMember);
        when(messagesRepository.getLastNMessagesInTheChat(1, firstChatId)).thenReturn(List.of(lastMessage));
        when(messagesRepository.getLastNMessagesInTheChat(1, secondChatId)).thenReturn(List.of());

        ChatListResponse result = chatService.getChats(userId);

        assertEquals(2, result.getItems().size());

        ChatSummary firstChat = result.getItems().stream()
                .filter(chat -> chat.getId().equals(firstChatId))
                .findFirst().orElseThrow();
        ChatSummary secondChat = result.getItems().stream()
                .filter(chat -> chat.getId().equals(secondChatId))
                .findFirst().orElseThrow();

        List<UserSummary> expectedFirst = List.of(new UserSummary(user.id(), user.username()), new UserSummary(firstMember.id(), firstMember.username()));

        assertEquals(expectedFirst, firstChat.getMembers());
        assertEquals(lastMessage.id(), firstChat.getLastMessage().get().getId());
        assertEquals(lastMessage.chatID(), firstChat.getLastMessage().get().getChatId());
        assertEquals(lastMessage.userID(), firstChat.getLastMessage().get().getUserId());
        assertEquals(lastMessage.content(), firstChat.getLastMessage().get().getContent());
        assertEquals(lastMessage.createdAt().toInstant(), firstChat.getLastMessage().get().getCreatedAt().toInstant());

        List<UserSummary> expectedSecond = List.of(new UserSummary(user.id(), user.username()), new UserSummary(secondMember.id(), secondMember.username()));
        assertEquals(expectedSecond, secondChat.getMembers());
        assertNull(secondChat.getLastMessage().get());
    }

    @Test
    void getChats_repositoryThrowsSQLException_throwsDatabaseException() throws SQLException {
        long userId = 1L;
        SQLException repositoryException = new SQLException("Database is unavailable");

        when(chatMembersRepository.getAllChatsOfTheMember(userId)).thenThrow(repositoryException);

        DatabaseException result = assertThrows(DatabaseException.class, () -> chatService.getChats(userId));

        assertSame(repositoryException, result.getCause());
    }
}
