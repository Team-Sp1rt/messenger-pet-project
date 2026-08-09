package messenger.backend.services;

import messenger.backend.exceptions.UserAlreadyExistsException;
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
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private AuthorisationRepository authorisationRepository;

    @Mock
    private UserRepository userRepository;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(authorisationRepository, userRepository, passwordEncoder);
    }

    @Test
    void shouldRegisterUserSuccessfully() throws SQLException {
        when(authorisationRepository.insertNewAuthorisationReturnsUserID(eq("newuser"), anyString()))
                .thenReturn(42L);

        registrationService.registerUser("newuser", "password123", "New User", LocalDate.of(2000, 1, 1));

        verify(userRepository).insertNewUser(42L, "New User", LocalDate.of(2000, 1, 1));
    }

    @Test
    void shouldHashPasswordBeforeInserting() throws SQLException {
        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenReturn(1L);

        registrationService.registerUser("newuser", "password123", "New User", LocalDate.of(2000, 1, 1));

        verify(authorisationRepository).insertNewAuthorisationReturnsUserID(eq("newuser"), argThat(hash ->
                !hash.equals("password123") && hash.startsWith("$2a$")
        ));
    }

    @Test
    void shouldThrowUserAlreadyExistsWhenLoginDuplicated() throws SQLException {
        SQLException duplicateKeyException = new SQLException("duplicate key value violates unique constraint", "23505");

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(duplicateKeyException);

        assertThrows(UserAlreadyExistsException.class, () ->
                registrationService.registerUser("existinguser", "password123", "Someone", LocalDate.of(2000, 1, 1))
        );
    }

    @Test
    void shouldThrowRuntimeExceptionForOtherSqlErrors() throws SQLException {
        SQLException connectionException = new SQLException("connection refused", "08001");

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(connectionException);

        assertThrows(RuntimeException.class, () ->
                registrationService.registerUser("someuser", "password123", "Someone", LocalDate.of(2000, 1, 1))
        );
    }
}