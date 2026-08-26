package messenger.backend.exceptions.services;

import messenger.backend.dtos.WebSocketErrorCode;

public class WebsocketServiceException extends RuntimeException {
  private final WebSocketErrorCode code;

  public WebsocketServiceException(WebSocketErrorCode code,
                                   String message, Throwable cause) {
      super(message, cause);
      this.code = code;
  }

    public WebsocketServiceException(WebSocketErrorCode code,
                                     String message) {
        super(message);
        this.code = code;
    }

  public WebSocketErrorCode getCode() {
      return code;
  }
}
