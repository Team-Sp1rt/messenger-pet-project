package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;
import org.springframework.http.HttpStatus;

public class UsernameAlreadyExistsException extends DatabaseException {
    public UsernameAlreadyExistsException(String message) {
        super("USERNAME_ALREADY_EXISTS", HttpStatus.CONFLICT, message);
    }
}