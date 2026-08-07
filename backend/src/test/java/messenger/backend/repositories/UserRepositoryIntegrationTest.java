package messenger.backend.repositories;

import messenger.backend.dtos.User;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

    @Container
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:16")
            .withDatabaseName("messenger")
            .withUsername("messenger")
            .withPassword("messenger")
            .withInitScripts("01_authorisation.sql", "02_users.sql");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthorisationRepository authorisationRepository;

    @Autowired
    DataSource dataSource;

    @BeforeEach
    void cleanDatabase() throws SQLException {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("TRUNCATE TABLE users, authorisation RESTART IDENTITY CASCADE");
        }
    }

    private long givenDBWithUser(String username, LocalDate birthday) throws SQLException {
        long id = authorisationRepository.insertNewAuthorisationReturnsUserID("meow", "meow-meow");
        userRepository.insertNewUser(id, username, birthday);
        return id;
    }

    private void thenUserDataShouldBeCorrect(User user, long id, String username, String bio, LocalDate birthday) {
        assertEquals(id, user.id());
        assertEquals(username, user.username());
        assertEquals(bio, user.bio());
        assertEquals(birthday, user.birthday());
    }

    @Test
    void insertNewUser_thenGetUserByID_returnsCorrectData() throws SQLException {
        long id = givenDBWithUser("meow", LocalDate.of(2000, 1, 1));

        User user = userRepository.getUserByID(id);

        thenUserDataShouldBeCorrect(user, id, "meow", null, LocalDate.of(2000, 1, 1));
    }

    @Test
    void changeUserBioByID_thenGetUserByID_returnsCorrcetData() throws SQLException {
        long id = givenDBWithUser("meow", LocalDate.of(2000, 1, 1));
        userRepository.changeUserBioByID(id, "meow-meow-meow");

        User user = userRepository.getUserByID(id);

        thenUserDataShouldBeCorrect(user, id, "meow", "meow-meow-meow", LocalDate.of(2000, 1, 1));
    }

    @Test
    void getUserByIDTest_incorrectID_throwsSQLException() {
        assertThrows(SQLException.class, () -> userRepository.getUserByID(13));
    }
}
