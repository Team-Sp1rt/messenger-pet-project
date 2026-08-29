package messenger.backend.repositories;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.NoSuchChatException;
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
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class ChatMembersRepositoryIntegrationTest {
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
    ChatMembersRepository chatMembersRepository;
    @Autowired
    DataSource dataSource;

    private final int FIRST_USER = 0;
    private final int SECOND_USER = 1;
    private final int FIRST_CHAT = 2;
    private final int SECOND_CHAT = 3;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE authorisation, users, chat_members, chats, messages RESTART IDENTITY");
        }
    }

    @Test
    void insertNewChatMember_thenGetAllMembersOfTheChat_returnsSetWithCorrectData() throws SQLException, NoSuchChatException {
        List<Long> ids = givenFilledTablesReturnsListOfIDs();

        chatMembersRepository.insertNewChatMember(ids.get(FIRST_CHAT), ids.get(FIRST_USER));
        chatMembersRepository.insertNewChatMember(ids.get(FIRST_CHAT), ids.get(SECOND_USER));
        Set<Long> membersOfTheChat = chatMembersRepository.getAllMembersOfTheChat(ids.get(FIRST_CHAT));

        assertEquals(Set.of(ids.get(FIRST_USER), ids.get(SECOND_USER)), membersOfTheChat);
    }

    @Test
    void insertNewChatMember_thenGetAllChatsOfTheMember_returnsSetWithCorrectData() throws SQLException {
        List<Long> ids = givenFilledTablesReturnsListOfIDs();

        chatMembersRepository.insertNewChatMember(ids.get(FIRST_CHAT), ids.get(FIRST_USER));
        chatMembersRepository.insertNewChatMember(ids.get(SECOND_CHAT), ids.get(FIRST_USER));
        Set<Long> membersOfTheChat = chatMembersRepository.getAllChatsOfTheMember(ids.get(FIRST_USER));

        assertEquals(Set.of(ids.get(FIRST_CHAT), ids.get(SECOND_CHAT)), membersOfTheChat);
    }

    @Test
    void insertingNewChatMember_insertingSameDataTwice_throwsSQLException() throws SQLException {
        List<Long> ids = givenFilledTablesReturnsListOfIDs();

        assertThrows(SQLException.class, () -> {
            chatMembersRepository.insertNewChatMember(ids.get(FIRST_CHAT), ids.get(FIRST_USER));
            chatMembersRepository.insertNewChatMember(ids.get(FIRST_CHAT), ids.get(FIRST_USER));
        });
    }

    @Test
    void getAllMembersOfTheChat_thereIsNoMembersInChat_throwsNoSuchChatException() {
        assertThrows(NoSuchChatException.class, () -> chatMembersRepository.getAllMembersOfTheChat(1L));
    }

    private List<Long> givenFilledTablesReturnsListOfIDs() throws SQLException {
        Long firstUserID = authorisationRepository.insertNewAuthorisationReturnsUserID("user1", "user1");
        userRepository.insertNewUser(new User(firstUserID, "user1", null, LocalDate.of(1,1,1)));
        Long secondUserID = authorisationRepository.insertNewAuthorisationReturnsUserID("user2", "user2");
        userRepository.insertNewUser(new User(secondUserID, "user2", null, LocalDate.of(1,1,1)));
        Long firstChatID = chatsRepository.insertNewChatReturnsChatID();
        Long secondChatID = chatsRepository.insertNewChatReturnsChatID();
        return new ArrayList<>(List.of(firstUserID, secondUserID, firstChatID, secondChatID));
    }
}
