package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;
import org.springframework.http.HttpStatus;

public class LoginAlreadyExistsException extends DatabaseException {
    public LoginAlreadyExistsException(String message) {
        super("LOGIN_ALREADY_EXISTS", HttpStatus.CONFLICT, message);
    }
}