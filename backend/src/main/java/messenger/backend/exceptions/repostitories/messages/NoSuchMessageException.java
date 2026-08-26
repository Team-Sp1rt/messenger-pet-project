package messenger.backend.exceptions.repostitories.messages;

import messenger.backend.exceptions.repostitories.ReposException;

public class NoSuchMessageException extends ReposException {
    public NoSuchMessageException(String message) { super(message); }
}
