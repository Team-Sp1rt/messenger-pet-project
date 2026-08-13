package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.InvalidCredentialsException;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {
    @Mock
    private AuthorisationRepository authorisationRepository;

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthorisationService authorisationService;

    @BeforeEach
    void setUp() {
        authorisationService = new AuthorisationService(authorisationRepository, userRepository, passwordEncoder);
    }

    @Test
    void getUserByLoginAndPassword_correctPassword_returnsUser() throws SQLException {
        String rawPassword = "password123";
        String hash = passwordEncoder.encode(rawPassword);

        when(authorisationRepository.getUserIDByLogin("testuser")).thenReturn(1L);
        when(authorisationRepository.getPasswordHashByLogin("testuser")).thenReturn(hash);
        when(userRepository.getUserByID(1L)).thenReturn(new User(1L, "Test", "", null));

        User result = authorisationService.getUserByLoginAndPassword("testuser", rawPassword);

        assertNotNull(result);
    }

    @Test
    void getUserByLoginAndPassword_wrongPassword_throwsInvalidCredentialsException() throws SQLException {
        String hash = passwordEncoder.encode("correctPassword");

        when(authorisationRepository.getUserIDByLogin("testuser")).thenReturn(1L);
        when(authorisationRepository.getPasswordHashByLogin("testuser")).thenReturn(hash);

        assertThrows(InvalidCredentialsException.class, () ->
                authorisationService.getUserByLoginAndPassword("testuser", "wrongPassword")
        );
    }

    @Test
    void getUserByLoginAndPassword_nonExistentLogin_throwsInvalidCredentialsException() throws SQLException {
        when(authorisationRepository.getUserIDByLogin("ghost"))
                .thenThrow(new SQLException("Couldn't get user id due to unknown reason"));

        assertThrows(InvalidCredentialsException.class, () ->
                authorisationService.getUserByLoginAndPassword("ghost", "anyPassword")
        );
    }
}