package messenger.backend.exceptions.services.chats;

import messenger.backend.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class InvalidMessageCursorException extends AppException {

    public InvalidMessageCursorException(String message, Throwable cause) {
        super("INVALID_MESSAGE_CURSOR", HttpStatus.BAD_REQUEST, message, cause);
    }
}
