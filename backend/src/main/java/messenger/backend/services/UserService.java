package messenger.backend.services;

import messenger.backend.generated.model.UserSearchResponse;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    // username - ищем именно как вхождение подстроки
    // limit - максимально кол-во результатов, которое хотим вернуть
    // проверка адекватности значения limit уже прописана в генерации

    // p.s скорее всего тебе потребуется использовать готовое dto UserSummary
    public UserSearchResponse searchUsers(String username, Integer limit) {
        return new UserSearchResponse();
    }
}
