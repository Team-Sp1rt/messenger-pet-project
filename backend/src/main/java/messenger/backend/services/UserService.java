package messenger.backend.services;

import messenger.backend.exceptions.repostitories.users.NoSuchUserException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.generated.model.UserSearchResponse;
import messenger.backend.repositories.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    //TODO: сделать нормальную сортировку (когда сделаешь, нужно удалить у метода сортировку из бд)
    public UserSearchResponse searchUsers(String username, Integer limit) {
        try {
            return new UserSearchResponse(userRepository.getNUserSummariesOfUsersWithSubstringInUsername(username, limit));
        } catch (SQLException e) {
            throw new DatabaseException("searchUsers failed due to SQLException: ", e);
        } catch (NoSuchUserException e) {
            throw new DatabaseException(
                    "NO_SUCH_USER", HttpStatus.NOT_FOUND,
                    "searchUsers failed due to NoSuchUserException: " + e.getMessage(), e
            );
        }
    }
}
