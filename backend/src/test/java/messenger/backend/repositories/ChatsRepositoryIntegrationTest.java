package messenger.backend.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import javax.sql.DataSource;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Testcontainers
public class ChatsRepositoryIntegrationTest {
    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("messenger")
            .withUsername("messenger")
            .withPassword("messenger")
            .withInitScript("03_chats.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    ChatsRepository chatsRepository;

    @Autowired
    DataSource dataSource;

    @Test
    void insertNewChatReturnsChatID_insertingToChats_IDsMustBeIncrementing() throws SQLException {
        long firstID = chatsRepository.insertNewChatReturnsChatID();
        long secondID = chatsRepository.insertNewChatReturnsChatID();

        assertEquals(firstID + 1, secondID);
    }
}
