package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.exceptions.services.auth.LoginAlreadyExistsException;
import messenger.backend.exceptions.services.auth.UsernameAlreadyExistsException;
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
    void registerUser_validData_createsUserAndReturnsAuthResponse() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("New User", "newuser", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(eq("newuser"), anyString())).thenReturn(42L);
        when(jwtService.generateAccessToken(42L, "New User")).thenReturn("test-token");

        AuthResponse response = registrationService.registerUser(request);

        User expectedUser = new User(42L, "New User", "", LocalDate.of(2000, 1, 1));

        assertEquals("test-token", response.token());
        assertEquals(expectedUser, response.user());

        verify(authorisationRepository).insertNewAuthorisationReturnsUserID(eq("newuser"), anyString());
        verify(userRepository).insertNewUser(expectedUser);
        verify(jwtService).generateAccessToken(42L, "New User");
    }

    @Test
    void registerUser_passwordIsHashedBeforeSaving() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("New User", "newuser", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString())).thenReturn(1L);
        when(jwtService.generateAccessToken(1L, "New User")).thenReturn("test-token");

        registrationService.registerUser(request);

        verify(authorisationRepository).insertNewAuthorisationReturnsUserID(eq("newuser"), argThat(hash ->
                !hash.equals("password123") && hash.startsWith("$2a$")
        ));
    }

    @Test
    void registerUser_duplicateLogin_throwsLoginAlreadyExistsException() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("New User", "existing-login", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(new SQLException("duplicate key", "23505"));

        assertThrows(LoginAlreadyExistsException.class, () -> registrationService.registerUser(request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerUser_duplicateUsername_throwsUsernameAlreadyExistsException() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("Existing User", "new-login", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString())).thenReturn(42L);

        doThrow(new SQLException("duplicate key", "23505"))
                .when(userRepository).insertNewUser(any(User.class));

        assertThrows(UsernameAlreadyExistsException.class, () -> registrationService.registerUser(request));

        verify(userRepository).insertNewUser(any(User.class));
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerUser_authorisationDatabaseError_throwsDatabaseException() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("New User", "newuser", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString()))
                .thenThrow(new SQLException("connection refused", "08001"));

        assertThrows(DatabaseException.class, () -> registrationService.registerUser(request));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(jwtService);
    }

    @Test
    void registerUser_userDatabaseError_throwsDatabaseException() throws SQLException {
        RegistrationRequest request = new RegistrationRequest("New User", "newuser", "password123", LocalDate.of(2000, 1, 1));

        when(authorisationRepository.insertNewAuthorisationReturnsUserID(anyString(), anyString())).thenReturn(42L);

        doThrow(new SQLException("connection refused", "08001"))
                .when(userRepository).insertNewUser(any(User.class));

        assertThrows(DatabaseException.class, () -> registrationService.registerUser(request));

        verify(jwtService, never()).generateAccessToken(anyLong(), anyString());
    }
}