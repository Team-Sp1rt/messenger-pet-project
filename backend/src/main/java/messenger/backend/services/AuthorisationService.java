package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.AuthorisationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.exceptions.services.auth.InvalidCredentialsException;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuthorisationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    private record UserCredentials(long userId, String passwordHash) {}

    public AuthorisationService(AuthorisationRepository authorisationRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private UserCredentials getCredentials(String login) {
        try {
            long userId = authorisationRepository.getUserIDByLogin(login);
            String passwordHash = authorisationRepository.getPasswordHashByLogin(login);

            return new UserCredentials(userId, passwordHash);
        } catch (SQLException e) {
            throw new InvalidCredentialsException("Invalid login or password", e);
        }
    }

    private void validatePassword(String password, String passwordHash) {
        if (!passwordEncoder.matches(password, passwordHash)) {
            throw new InvalidCredentialsException("Invalid login or password");
        }
    }

    private User getUser(long userId) {
        try {
            return userRepository.getUserByID(userId);
        } catch (SQLException e) {
            throw new DatabaseException("Failed to retrieve user", e);
        }
    }

    public AuthResponse getUserByLoginAndPassword(AuthorisationRequest request) {
        UserCredentials credentials = getCredentials(request.login());

        validatePassword(request.password(), credentials.passwordHash());

        User user = getUser(credentials.userId());

        String token = jwtService.generateAccessToken(credentials.userId(), user.username());

        return new AuthResponse(token, user);
    }
}
