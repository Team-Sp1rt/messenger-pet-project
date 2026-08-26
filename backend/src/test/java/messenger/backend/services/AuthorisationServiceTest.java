package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.users.NoSuchUserException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.exceptions.services.auth.InvalidCredentialsException;
import messenger.backend.dtos.requests.AuthorisationRequest;
import messenger.backend.dtos.responses.AuthResponse;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthorisationServiceTest {
    @Mock
    private AuthorisationRepository authorisationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private AuthorisationService authorisationService;

    @BeforeEach
    void setUp() {
        authorisationService = new AuthorisationService(authorisationRepository, userRepository, passwordEncoder, jwtService);
    }

    //TODO: тест на кидание DatabaseException, который кидается после NoSuchUserException

    @Test
    void getUserByLoginAndPassword_correctPassword_returnsUserAndToken() throws SQLException, NoSuchUserException {
        String rawPassword = "password123";
        String hash = passwordEncoder.encode(rawPassword);
        User user = new User(1L, "Test", "", null);

        when(authorisationRepository.getUserIDByLogin("testuser")).thenReturn(1L);
        when(authorisationRepository.getPasswordHashByLogin("testuser")).thenReturn(hash);
        when(userRepository.getUserByID(1L)).thenReturn(user);
        when(jwtService.generateAccessToken(1L, "Test")).thenReturn("test-token");

        AuthResponse result = authorisationService.getUserByLoginAndPassword(new AuthorisationRequest("testuser", rawPassword));

        assertEquals(user, result.user());
        assertEquals("test-token", result.token());
    }

    @Test
    void getUserByLoginAndPassword_wrongPassword_throwsInvalidCredentialsException() throws SQLException {
        String hash = passwordEncoder.encode("correctPassword");

        when(authorisationRepository.getUserIDByLogin("testuser")).thenReturn(1L);
        when(authorisationRepository.getPasswordHashByLogin("testuser")).thenReturn(hash);

        assertThrows(InvalidCredentialsException.class, () ->
                authorisationService.getUserByLoginAndPassword(new AuthorisationRequest("testuser", "wrongPassword"))
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void getUserByLoginAndPassword_nonExistentLogin_throwsInvalidCredentialsException() throws SQLException {
        when(authorisationRepository.getUserIDByLogin("ghost"))
                .thenThrow(new SQLException("User not found"));

        assertThrows(InvalidCredentialsException.class, () ->
                authorisationService.getUserByLoginAndPassword(new AuthorisationRequest("ghost", "anyPassword"))
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void getUserByLoginAndPassword_databaseErrorWhileGettingCredentials_throwsInvalidCredentialsException() throws SQLException {
        when(authorisationRepository.getUserIDByLogin("testuser"))
                .thenThrow(new SQLException("Connection refused", "08001"));

        assertThrows(InvalidCredentialsException.class, () ->
                authorisationService.getUserByLoginAndPassword(new AuthorisationRequest("testuser", "password123"))
        );

        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void getUserByLoginAndPassword_databaseErrorWhileGettingUser_throwsDatabaseException() throws SQLException, NoSuchUserException {
        String hash = passwordEncoder.encode("password123");

        when(authorisationRepository.getUserIDByLogin("testuser")).thenReturn(1L);
        when(authorisationRepository.getPasswordHashByLogin("testuser")).thenReturn(hash);
        when(userRepository.getUserByID(1L))
                .thenThrow(new SQLException("Connection refused", "08001"));

        assertThrows(DatabaseException.class, () ->
                authorisationService.getUserByLoginAndPassword(new AuthorisationRequest("testuser", "password123"))
        );

        verifyNoInteractions(jwtService);
    }
}