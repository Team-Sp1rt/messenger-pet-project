package messenger.backend.exceptions.services.auth;

import messenger.backend.exceptions.services.DatabaseException;

public class UserAlreadyExistsException extends DatabaseException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}