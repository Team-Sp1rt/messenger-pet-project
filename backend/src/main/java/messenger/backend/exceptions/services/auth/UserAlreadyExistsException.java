package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;
import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends DatabaseException {
    public UserAlreadyExistsException(String message) {
        super("USER_ALREADY_EXISTS", HttpStatus.CONFLICT, message);
    }
}