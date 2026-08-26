package messenger.backend.services;

import messenger.backend.exceptions.repostitories.users.NoSuchUserException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository);
    }

    @Test
    void searchUsers_returnsCorrectUserSummariesList() throws SQLException, NoSuchUserException {
        userService.searchUsers("moo", 13);
        verify(userRepository).getNUserSummariesOfUsersWithSubstringInUsername("moo", 13);
    }

    @Test
    void searchUsers_unexpectedSQLException_throwsDatabaseException() throws SQLException, NoSuchUserException {
        SQLException connectionException = new SQLException("connection refused", "08001");

        when(userRepository.getNUserSummariesOfUsersWithSubstringInUsername(anyString(), anyInt()))
                .thenThrow(connectionException);

        assertThrows(DatabaseException.class, () ->
                userService.searchUsers("moo", 5)
        );
    }
}