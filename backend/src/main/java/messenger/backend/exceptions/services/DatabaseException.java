package messenger.backend.exceptions.services;

import messenger.backend.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class DatabaseException extends AppException {
    public DatabaseException(String message) {
        super("DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, message);
    }
    public DatabaseException(String message, Throwable cause) {
        super("DATABASE_ERROR", HttpStatus.INTERNAL_SERVER_ERROR, message, cause);
    }

    protected DatabaseException(String errorCode, HttpStatus status, String message) {
        super(errorCode, status, message);
    }
    protected DatabaseException(String errorCode, HttpStatus status, String message, Throwable cause) {
        super(errorCode, status, message, cause);
    }
}