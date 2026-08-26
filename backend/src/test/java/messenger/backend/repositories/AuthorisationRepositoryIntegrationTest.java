package messenger.backend.repositories;

import messenger.backend.exceptions.repostitories.ReposException;
import messenger.backend.exceptions.repostitories.NoSuchUserException;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
public class AuthorisationRepositoryIntegrationTest {
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
    DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE authorisation, users, chat_members, chats, messages RESTART IDENTITY");
        }
    }

    @Test
    void insertNewAuthorization_thenGetUserIDByLoginAndGetPasswordHashByLogin_returnsCorrectData() throws SQLException, ReposException {
        String login = "meow";
        String passwordHash = "meow-meow";
        Long id = authorisationRepository.insertNewAuthorisationReturnsUserID(login, passwordHash);

        Long idFromDB = authorisationRepository.getUserIDByLogin(login);
        String passwordHashFromDB = authorisationRepository.getPasswordHashByLogin(login);

        assertEquals(id, idFromDB);
        assertEquals(passwordHash, passwordHashFromDB);
    }

    @Test
    void getUserIDByLogin_noUserWithSpecifiedLogin_throwsNoSuchUserException() {
        assertThrows(NoSuchUserException.class, () -> authorisationRepository.getUserIDByLogin("meow"));
    }

    @Test
    void getPasswordHashByLogin_noUserWithSpecifiedLogin_throwsNoSuchUserException() {
        assertThrows(NoSuchUserException.class, () -> authorisationRepository.getPasswordHashByLogin("meow"));
    }

    @Test
    void insertNewAuthorisation_SpecifiedLoginIsAlreadyInDB_throwsSQLException() {
        assertThrows(SQLException.class, () -> {
            authorisationRepository.insertNewAuthorisationReturnsUserID("meow", "meow-meow");
            authorisationRepository.insertNewAuthorisationReturnsUserID("meow", "meow-meow");
        });
    }
}
