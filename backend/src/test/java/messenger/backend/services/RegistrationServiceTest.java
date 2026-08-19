package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.exceptions.services.auth.UserAlreadyExistsException;
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

    @Mock
    private JwtService jwtService;

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private RegistrationService registrationService;

    @BeforeEach
    void setUp() {
        registrationService = new RegistrationService(
                authorisationRepository,
                userRepository,
                passwordEncoder,
                jwtService
        );
    }

    @Test
    void registerUser_validData_insertsUserWithReturnedId() throws SQLException {
        RegistrationRequest registerRequest = new RegistrationRequest(
                "New User",
                "newuser",
                "password123",
                LocalDate.of(2000, 1, 1)
        );

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(eq("newuser"), anyString()))
                .thenReturn(42L);

        when(jwtService.generateAccessToken(42L, "New User"))
                .thenReturn("test-token");

        registrationService.registerUser(registerRequest);

        User user = new User(42L, "New User", "", LocalDate.of(2000, 1, 1));
        verify(userRepository).insertNewUser(user);
    }

    @Test
    void registerUser_validPassword_storesHashedPasswordNotRawPassword() throws SQLException {
        RegistrationRequest registerRequest = new RegistrationRequest(
                "New User",
                "newuser",
                "password123",
                LocalDate.of(2000, 1, 1)
        );


        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenReturn(1L);

        when(jwtService.generateAccessToken(1L, "New User"))
                .thenReturn("test-token");

        registrationService.registerUser(registerRequest);

        verify(authorisationRepository).insertNewAuthorisationReturnsUserID(eq("newuser"), argThat(hash ->
                !hash.equals("password123") && hash.startsWith("$2a$")
        ));
    }

    @Test
    void registerUser_duplicateLogin_throwsUserAlreadyExistsException() throws SQLException {
        SQLException duplicateKeyException = new SQLException("duplicate key value violates unique constraint", "23505");

        RegistrationRequest registerRequest = new RegistrationRequest(
                "Someone",
                "existinguser",
                "password123",
                LocalDate.of(2000, 1, 1)
        );


        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(duplicateKeyException);

        assertThrows(UserAlreadyExistsException.class, () ->
                registrationService.registerUser(registerRequest)
        );
    }

    @Test
    void registerUser_unexpectedSqlError_throwsRuntimeException() throws SQLException {
        SQLException connectionException = new SQLException("connection refused", "08001");

        RegistrationRequest registerRequest = new RegistrationRequest(
                "Someone",
                "existinguser",
                "password123",
                LocalDate.of(2000, 1, 1)
        );

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(connectionException);

        assertThrows(RuntimeException.class, () ->
                registrationService.registerUser(registerRequest)
        );
    }
}