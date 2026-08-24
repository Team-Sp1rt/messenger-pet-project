package messenger.backend.exceptions.services.chats;

import messenger.backend.exceptions.AppException;
import org.springframework.http.HttpStatus;

public class ChatAccessDeniedException extends AppException {

    public ChatAccessDeniedException(String message) {
        super("CHAT_ACCESS_DENIED", HttpStatus.FORBIDDEN, message);
    }
}