package messenger.backend.exceptions.repostitories;

public class ReposException extends RuntimeException {
  public ReposException(String message) {
    super(message);
  }

  public ReposException(String message, Throwable cause) {
    super(message, cause);
  }
}
