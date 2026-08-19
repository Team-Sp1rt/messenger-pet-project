package messenger.backend.services;

import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.generated.model.UserSearchResponse;
import messenger.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // username - ищем именно как вхождение подстроки
    // limit - максимально кол-во результатов, которое хотим вернуть
    // проверка адекватности значения limit уже прописана в генерации

    // p.s скорее всего тебе потребуется использовать готовое dto UserSummary
    public UserSearchResponse searchUsers(String username, Integer limit) {
        try {
            return new UserSearchResponse(userRepository.getNUserSummariesStartingWithSubstring(username, limit));
        } catch (SQLException e) {
            throw new DatabaseException("searchUsers failed due to SQLException: ", e);
        }
    }
}
