package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.exceptions.InvalidCredentialsException;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuthorisationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthorisationService(AuthorisationRepository authorisationRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByLoginAndPassword(String login, String password) {
        try {
            long userID = authorisationRepository.getUserIDByLogin(login);
            String passwordHash = authorisationRepository.getPasswordHashByLogin(login);

            if (!passwordEncoder.matches(password, passwordHash)) {
                throw new InvalidCredentialsException("Invalid username or password");
            }

            return userRepository.getUserByID(userID);
        } catch (SQLException e) {
            throw new InvalidCredentialsException("Invalid username or password", e);
        }
    }
}
