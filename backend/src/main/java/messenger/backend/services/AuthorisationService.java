package messenger.backend.services;

import messenger.backend.dtos.User;
import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class AuthorisationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;

    public AuthorisationService(AuthorisationRepository authorisationRepository, UserRepository userRepository) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
    }

    //TODO: Андрей, сделай обработчик SQLException, не кидай его выше
    public User getUserByLogin(String login) throws SQLException {
        long userID = authorisationRepository.getUserIDByLogin(login);
        return userRepository.getUserByID(userID);
    }
}
