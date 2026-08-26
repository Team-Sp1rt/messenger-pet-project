package messenger.backend.exceptions.repostitories.users;

import messenger.backend.exceptions.repostitories.ReposException;

public class NoSuchUserException extends ReposException {
    public NoSuchUserException(String message) { super(message); }
}
