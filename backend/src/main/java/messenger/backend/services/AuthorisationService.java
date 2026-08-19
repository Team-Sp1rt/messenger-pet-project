package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.AuthorisationRequest;
import messenger.backend.dtos.responses.AuthResponse;
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

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    public AuthorisationService(AuthorisationRepository authorisationRepository,
                                UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                JwtService jwtService) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public AuthResponse getUserByLoginAndPassword(AuthorisationRequest authorisationRequest) {
        try {
            long userID = authorisationRepository.getUserIDByLogin(authorisationRequest.login());
            String passwordHash = authorisationRepository.getPasswordHashByLogin(authorisationRequest.login());

            if (!passwordEncoder.matches(authorisationRequest.password(), passwordHash)) {
                throw new InvalidCredentialsException("Invalid username or password");
            }

            User user = userRepository.getUserByID(userID);

            String token = jwtService.generateAccessToken(userID, user.username());

            return new AuthResponse(token, user);
        } catch (SQLException e) {
            throw new InvalidCredentialsException("Invalid username or password", e);
        }
    }
}
