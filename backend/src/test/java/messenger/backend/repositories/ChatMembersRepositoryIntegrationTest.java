package messenger.backend.repositories;

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
            .withPassword("messenger")
            .withInitScripts("01_authorisation.sql", "02_users.sql", "03_chats.sql", "04_chat_members.sql");

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

    private final int USER = 0;
    private final int CHAT = 1;


    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE authorisation, users, chats, chat_members RESTART IDENTITY CASCADE");
        }
    }

    private List<Long> givenFilledTabelsReturnsListOfIDs() throws SQLException {
        long userID = authorisationRepository.insertNewAuthorisationReturnsUserID("user1", "user1");
        userRepository.insertNewUser(userID, "user1", LocalDate.of(1,1,1));
        long chatID = chatsRepository.insertNewChatReturnsChatID();
        return List.of(userID, chatID);
    }

    @Test
    void insertNewChatMember_thenGetAllMembersOfTheChat_returnsSetWithCorrectData() throws SQLException {
        List<Long> ids = givenFilledTabelsReturnsListOfIDs();

        chatMembersRepository.insertNewChatMember(ids.get(USER), ids.get(CHAT));
        Set<Long> membersOfTheChat = chatMembersRepository.getAllMembersOfTheChat(ids.get(CHAT));

        assertEquals(Set.of(ids.get(USER)), membersOfTheChat);
    }

    @Test
    void getAllMembersOfTheChat_thereIsNoMembersInSpecifiedChat_throwsSQLException() {
        assertThrows(SQLException.class, () -> chatMembersRepository.getAllMembersOfTheChat(1));
    }
}
