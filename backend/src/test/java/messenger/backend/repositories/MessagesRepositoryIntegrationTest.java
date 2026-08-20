package messenger.backend.repositories;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.NewMessage;
import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.messages.NoSuchMessageException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class MessagesRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("messenger")
            .withUsername("messenger")
            .withPassword("messenger");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    AuthorisationRepository authorisationRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    ChatsRepository chatsRepository;
    @Autowired
    MessagesRepository messagesRepository;
    @Autowired
    DataSource dataSource;

    private final int FIRST_USER = 0;
    private final int SECOND_USER = 1;
    private final int FIRST_CHAT = 2;
    private final int SECOND_CHAT = 3;
    private final int FIRST_MESSAGE = 4;
    private final int SECOND_MESSAGE = 5;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE authorisation, users, chat_members, chats, messages RESTART IDENTITY");
        }
    }

    @Test
    void insertNewMessageReturnsMessage_returnsCorrectMessage()
            throws SQLException {
        List<Long> ids = givenFilledOtherTablesReturnsListOfIDs();
        String content = "meow";

        Message message = messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(FIRST_CHAT), ids.get(FIRST_USER), content)
        );

        thenMessageShouldBeCorrect(message, ids.get(FIRST_CHAT), ids.get(FIRST_USER), content);
    }

    @Test
    void insertNewMessageReturnsMessage_insertingTwoMessages_returnsMessagesWithIncreasingIDsAndCreatedAt()
            throws SQLException {
        List<Long> ids = givenFilledOtherTablesReturnsListOfIDs();
        String content = "meow";

        Message firstMessage = messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(FIRST_CHAT), ids.get(FIRST_USER), content)
        );
        Message secondMessage = messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(FIRST_CHAT), ids.get(FIRST_USER), content)
        );

        thenIDsAndCreatedAtShouldBeIncreasing(firstMessage, secondMessage);
    }

    @Test
    void getLastNMessagesInTheChat_ReturnsCorrectMessagesList()
            throws SQLException {
        List<Long> ids = givenFilledAllTablesReturnsListOfIDs();

        List<Message> messagesList = messagesRepository.getLastNMessagesInTheChat(10, ids.get(FIRST_CHAT));

        thenMessagesListShouldBeCorrect(messagesList, ids);
    }

    @Test
    void getNMessagesInTheChatBeforeMessage_ReturnsCorrectMessagesList()
            throws SQLException {
        List<Long> ids = givenFilledAllTablesReturnsListOfIDs();

        List<Message> messagesList = messagesRepository.getNMessagesInTheChatBeforeMessage(
                10, ids.get(FIRST_CHAT),
                new Message(null, null, null, null, Timestamp.from(Instant.now()))
                );

        thenMessagesListShouldBeCorrect(messagesList, ids);
    }

    @Test
    void getUserIDByMessageID_ReturnsCorrectUserID()
            throws SQLException {
        List<Long> ids = givenFilledAllTablesReturnsListOfIDs();

        Long userID = messagesRepository.getUserIDByMessageID(ids.get(SECOND_MESSAGE));

        assertEquals(ids.get(SECOND_USER), userID);
    }

    @Test
    void getUserIDByMessageID_noMessageWithSuchID_ThrowsNoSuchMessageException()
            throws SQLException {
        assertThrows(NoSuchMessageException.class, () -> messagesRepository.getUserIDByMessageID(13L));
    }

    @Test
    void editMessageReturnsMessage_returnsCorrectMessage()
            throws SQLException {
        List<Long> ids = givenFilledAllTablesReturnsListOfIDs();
        String newContent = "woof";

        Message message = messagesRepository.editMessageReturnsMessage(ids.get(FIRST_MESSAGE), newContent);

        thenMessageShouldBeCorrect(message, ids.get(FIRST_CHAT), ids.get(FIRST_USER), newContent);
    }

    @Test
    void editMessageReturnsMessage_editingNonExistingMessage_throwsSQLException() {
        assertThrows(SQLException.class, () -> messagesRepository.editMessageReturnsMessage(1L, "woof"));
    }

    @Test
    void deleteMessageReturnsMessage_thenGetLastNMessagesInTheChat_returnsMessagesListWithoutDeletedOne()
            throws SQLException {
        List<Long> ids = givenFilledAllTablesReturnsListOfIDs();

        messagesRepository.deleteMessage(ids.get(SECOND_MESSAGE));
        List<Message> messagesList = messagesRepository.getLastNMessagesInTheChat(10, ids.get(FIRST_CHAT));

        thenMessagesListShouldNotContainMessageWithID(messagesList, ids.get(SECOND_MESSAGE));
    }

    @Test
    void deleteMessage_deletingNonExistingMessage_doesNotThrow() {
        assertDoesNotThrow(() -> messagesRepository.deleteMessage(1L));
    }

    private List<Long> givenFilledAllTablesReturnsListOfIDs()
            throws SQLException {
        List<Long> ids = givenFilledOtherTablesReturnsListOfIDs();
        String content = "meow";

        ids.add(messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(FIRST_CHAT), ids.get(FIRST_USER), content)
        ).id());
        ids.add(messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(FIRST_CHAT), ids.get(SECOND_USER), content)
        ).id());
        ids.add(messagesRepository.insertNewMessageReturnsMessage(
                new NewMessage(ids.get(SECOND_CHAT), ids.get(FIRST_USER), content)
        ).id());

        return ids;
    }

    private List<Long> givenFilledOtherTablesReturnsListOfIDs()
            throws SQLException {
        Long firstUserID = authorisationRepository.insertNewAuthorisationReturnsUserID("user1", "user1");
        userRepository.insertNewUser(new User(
                firstUserID,
                "user1",
                null,
                LocalDate.of(1,1,1))
        );
        Long secondUserID = authorisationRepository.insertNewAuthorisationReturnsUserID("user2", "user2");
        userRepository.insertNewUser(new User(
                secondUserID,
                "user2",
                null,
                LocalDate.of(1,1,1))
        );
        Long firstChatID = chatsRepository.insertNewChatReturnsChatID();
        Long secondChatID = chatsRepository.insertNewChatReturnsChatID();
        return new ArrayList<>(List.of(firstUserID, secondUserID, firstChatID, secondChatID));
    }

    private void thenMessageShouldBeCorrect(Message message, Long chatID, Long userID, String content) {
        assertEquals(chatID, message.chatID());
        assertEquals(userID, message.userID());
        assertEquals(content, message.content());
    }

    private void thenIDsAndCreatedAtShouldBeIncreasing(Message firstMessage, Message secondMessage) {
        assertTrue(firstMessage.id() < secondMessage.id());
        assertTrue(firstMessage.createdAt().before(secondMessage.createdAt()));
    }

    private void thenMessagesListShouldBeCorrect(List<Message> messageList, List<Long> ids) {
        assertEquals(2, messageList.size());
        String content = "meow";
        Message firstMessage = messageList.get(1);
        assertEquals(ids.get(FIRST_MESSAGE), firstMessage.id());
        assertEquals(ids.get(FIRST_CHAT), firstMessage.chatID());
        assertEquals(ids.get(FIRST_USER), firstMessage.userID());
        assertEquals(content, firstMessage.content());
        Message secondMessage = messageList.getFirst();
        assertEquals(ids.get(SECOND_MESSAGE), secondMessage.id());
        assertEquals(ids.get(FIRST_CHAT), secondMessage.chatID());
        assertEquals(ids.get(SECOND_USER), secondMessage.userID());
        assertEquals(content, secondMessage.content());
    }

    private void thenMessagesListShouldNotContainMessageWithID(List<Message> messageList, Long id) {
        assertTrue(messageList.stream().noneMatch(message -> message.id().equals(id)));
    }
}
