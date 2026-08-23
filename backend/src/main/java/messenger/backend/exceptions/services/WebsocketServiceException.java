package messenger.backend.exceptions.services;

import messenger.backend.dtos.WebSocketErrorCode;

public class WebsocketServiceException extends RuntimeException {
  private final WebSocketErrorCode code;
  private final String message;

  public WebsocketServiceException(WebSocketErrorCode code,
                                   String message) {
      this.code = code;
      this.message = message;
  }

  @Override
    public String getMessage() {
      return message;
  }

  public WebSocketErrorCode getCode() {
      return code;
  }
}
