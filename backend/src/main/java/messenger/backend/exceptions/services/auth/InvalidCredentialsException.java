package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;
import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends DatabaseException {
    public InvalidCredentialsException(String message) {
        super("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super("INVALID_CREDENTIALS", HttpStatus.UNAUTHORIZED, message, cause);
    }
}