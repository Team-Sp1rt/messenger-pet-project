package messenger.backend.exceptions.repostitories;

public class NoSuchChatException extends ReposException {
    public NoSuchChatException(String message) {
        super(message);
    }
}
