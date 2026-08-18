package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.dtos.responses.UserResponse;
import messenger.backend.exceptions.UserAlreadyExistsException;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;

@Service
public class RegistrationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;

    private final PasswordEncoder passwordEncoder;
    private final JwtService JwtService;

    public RegistrationService(AuthorisationRepository authorisationRepository,
                               UserRepository userRepository,
                               PasswordEncoder passwordEncoder,
                               JwtService JwtService) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.JwtService = JwtService;
    }

    @Transactional
    public AuthResponse registerUser(String username, String login, String password, LocalDate birthday) {
        String passwordHash = passwordEncoder.encode(password);

        try {
            Long id = authorisationRepository.insertNewAuthorisationReturnsUserID(login, passwordHash);
            User user = new User(id, username, "", birthday);
            userRepository.insertNewUser(user);

            String token = JwtService.generateAccessToken(id, username);

            return new AuthResponse(
                    token,
                    new UserResponse(String.valueOf(user.id()), user.username())
            );
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new UserAlreadyExistsException("A user with this username already exists");
            } else {
                throw new RuntimeException("Failed to register the user", e);
            }
        }
    }
}
