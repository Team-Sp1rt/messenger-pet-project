package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;

public class InvalidCredentialsException extends DatabaseException {
    public InvalidCredentialsException(String message) {
        super(message);
    }

    public InvalidCredentialsException(String message, Throwable cause) {
        super(message, cause);
    }
}