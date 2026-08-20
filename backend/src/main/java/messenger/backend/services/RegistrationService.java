package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.exceptions.services.auth.LoginAlreadyExistsException;
import messenger.backend.exceptions.services.auth.UsernameAlreadyExistsException;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
public class RegistrationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public RegistrationService(AuthorisationRepository authorisationRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService jwtService) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    private Long createAuthorisation(String login, String passwordHash) {
        try {
            return authorisationRepository.insertNewAuthorisationReturnsUserID(login, passwordHash);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new LoginAlreadyExistsException("A user with this login already exists");
            }

            throw new DatabaseException("Failed to create authorisation", e);
        }
    }

    private void createUser(User user) {
        try {
            userRepository.insertNewUser(user);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new UsernameAlreadyExistsException("A user with this username already exists");
            }

            throw new DatabaseException("Failed to create user", e);
        }
    }

    @Transactional
    public AuthResponse registerUser(RegistrationRequest registerRequest) {
        String passwordHash = passwordEncoder.encode(registerRequest.password());
        Long id = createAuthorisation(registerRequest.login(), passwordHash);

        User user = new User(id, registerRequest.username(),"", registerRequest.birthday());
        createUser(user);

        String token = jwtService.generateAccessToken(id, registerRequest.username());

        return new AuthResponse(token, user);
    }
}
