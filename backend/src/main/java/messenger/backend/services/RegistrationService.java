package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.exceptions.services.auth.UserAlreadyExistsException;
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

    @Transactional
    public AuthResponse registerUser(RegistrationRequest registerRequest) {
        String passwordHash = passwordEncoder.encode(registerRequest.password());

        //TODO: всё ещё плохая обработка.
        // Упадёт одинаково и когда login уже есть в таблице authorisation, и когда username есть в users
        try {
            Long id = authorisationRepository.insertNewAuthorisationReturnsUserID(registerRequest.login(), passwordHash);
            User user = new User(id, registerRequest.username(), "", registerRequest.birthday());
            userRepository.insertNewUser(user);

            String token = jwtService.generateAccessToken(id, registerRequest.username());

            return new AuthResponse(token, user);
        } catch (SQLException e) {
            if ("23505".equals(e.getSQLState())) {
                throw new UserAlreadyExistsException("A user with this username already exists");
            } else {
                throw new DatabaseException("Failed to register the user: ", e);
            }
        }
    }
}
