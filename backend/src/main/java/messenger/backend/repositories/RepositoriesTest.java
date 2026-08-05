package messenger.backend.repositories;

import messenger.backend.services.AuthorisationService;
import messenger.backend.services.RegistrationService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.time.LocalDate;

@Component
public class RepositoriesTest implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AuthorisationRepository authorisationRepository;
    private final ChatsRepository chatsRepository;
    private final RegistrationService registrationService;
    private final AuthorisationService authorisationService;

    public RepositoriesTest(UserRepository userRepository, AuthorisationRepository authorisationRepository, ChatsRepository chatsRepository, RegistrationService registrationService, AuthorisationService authorisationService) {
        this.userRepository = userRepository;
        this.authorisationRepository = authorisationRepository;
        this.chatsRepository = chatsRepository;
        this.registrationService = registrationService;
        this.authorisationService = authorisationService;
    }

    @Override
    public void run(String... args) throws Exception {
        userRepository.showEverything();
        authorisationRepository.showEverything();
        LocalDate localDate = LocalDate.of(2026, 8, 3);
        try {
            registrationService.registerUser("meow", "meow", "meow", localDate);
        }
        catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        userRepository.showEverything();
        authorisationRepository.showEverything();
        System.out.println(authorisationRepository.getPasswordHashByLogin("InsomniaDemon"));
        System.out.println(authorisationService.getUserByLogin("InsomniaDemon"));
        userRepository.changeUserBioByID(4, "I am gay");
        System.out.println(authorisationService.getUserByLogin("InsomniaDemon"));
    }
}
