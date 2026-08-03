package messenger.backend.repositories;

import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.time.LocalDate;

@Component
public class RepositoriesTest implements CommandLineRunner {
    private final UserRepository userRepository;
    private final AuthorisationRepository authorisationRepository;
    private final ChatsRepository chatsRepository;

    public RepositoriesTest(UserRepository userRepository, AuthorisationRepository authorisationRepository, ChatsRepository chatsRepository) {
        this.userRepository = userRepository;
        this.authorisationRepository = authorisationRepository;
        this.chatsRepository = chatsRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        userRepository.showEverything();
        authorisationRepository.showEverything();
        LocalDate localDate = LocalDate.of(2026, 8, 3);
        userRepository.createNewUser("meow", "meow", "meow", localDate);
        userRepository.showEverything();
        authorisationRepository.showEverything();
    }
}
