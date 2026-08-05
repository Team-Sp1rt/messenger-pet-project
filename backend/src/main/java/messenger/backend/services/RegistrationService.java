package messenger.backend.services;

import messenger.backend.repositories.AuthorisationRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.LocalDate;

@Service
public class RegistrationService {
    private final AuthorisationRepository authorisationRepository;
    private final UserRepository userRepository;

    public RegistrationService(AuthorisationRepository authorisationRepository, UserRepository userRepository) {
        this.authorisationRepository = authorisationRepository;
        this.userRepository = userRepository;
    }

    //TODO: Андрей, сделай обработчик SQLException, не кидай его выше
    @Transactional //Transactional делает метод атомарным, либо обе записи прошли, либо ничего не записалось
    public void registerUser(String login, String passwordHash, String username, LocalDate birthday) throws SQLException {
        long id = authorisationRepository.insertNewAuthorisationReturnsUserID(login, passwordHash);
        userRepository.insertNewUser(id, username, birthday);
    }
}
