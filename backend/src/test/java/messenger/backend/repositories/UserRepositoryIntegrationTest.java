package messenger.backend.repositories;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.users.NoSuchUserException;
import messenger.backend.generated.model.UserSummary;
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

import static org.junit.jupiter.api.Assertions.*;

//TODO: переделать нахуй тесты

@SpringBootTest
@Testcontainers
class UserRepositoryIntegrationTest {

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
    UserRepository userRepository;
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
    void insertNewUser_thenGetUserByID_returnsCorrectData() throws SQLException {
        Long id = givenUserInDB("meow", LocalDate.of(2000, 1, 1));

        User user = userRepository.getUserByID(id);

        assertEquals(user, new User(id, "meow", "", LocalDate.of(2000, 1, 1)));
    }

    @Test
    void getUserByIDTest_incorrectID_throwsNoSuchUserException() {
        assertThrows(NoSuchUserException.class, () -> userRepository.getUserByID(13L));
    }

    //Фу
    @Test
    void getNUserSummariesStartingWithSubstring_returnsCorrectSortedUserSummariesList() throws SQLException {
        Long[] ids = new Long[5];
        ids[0] = givenUserInDB("meow", LocalDate.of(2000, 1, 1), "1");
        ids[1] = givenUserInDB("meow-meow", LocalDate.of(2000, 1, 1), "2");
        ids[2] = givenUserInDB("123meow-meow", LocalDate.of(2000, 1, 1), "3");
        ids[3] = givenUserInDB("meow-meow-meow", LocalDate.of(2000, 1, 1), "4");
        ids[4] = givenUserInDB("moo", LocalDate.of(2000, 1, 1), "5");

        List<UserSummary> actualUserSummariesList = userRepository.getNUserSummariesOfUsersWithSubstringInUsername("meow", 5);

        List<UserSummary> expectedUserSummariesList = new ArrayList<>(List.of(
                new UserSummary(ids[2], "123meow-meow"),
                new UserSummary(ids[0], "meow"),
                new UserSummary(ids[1], "meow-meow"),
                new UserSummary(ids[3], "meow-meow-meow"))
        );
        assertEquals(expectedUserSummariesList, actualUserSummariesList);
    }

    @Test
    void getNUserSummariesStartingWithSubstring_noUsersWithSimilarUsername_throwsNoSuchUserException() {
        assertThrows(NoSuchUserException.class, () -> userRepository.getNUserSummariesOfUsersWithSubstringInUsername("oink", 13));
    }

    @Test
    void changeUserBioByID_thenGetUserByID_returnsCorrectData() throws SQLException {
        Long id = givenUserInDB("meow", LocalDate.of(2000, 1, 1));

        userRepository.changeUserBioByID(id, "meow-meow-meow");

        User actualUser = userRepository.getUserByID(id);
        User expectedUser = new User(id, "meow", "meow-meow-meow", LocalDate.of(2000, 1, 1));
        assertEquals(expectedUser, actualUser);
    }


    private Long givenUserInDB(String username, LocalDate birthday) throws SQLException {
        return givenUserInDB(username, birthday, "");
    }

    private Long givenUserInDB(String username, LocalDate birthday, String loginDifference) throws SQLException {
        Long id = authorisationRepository.insertNewAuthorisationReturnsUserID("meow" + loginDifference, "meow-meow");
        userRepository.insertNewUser(new User(id, username, "", birthday));
        return id;
    }
}
