package messenger.backend.controllers;

import messenger.backend.dtos.WebSocketError;
import messenger.backend.dtos.WebSocketErrorCode;
import messenger.backend.exceptions.services.WebsocketServiceException;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.support.MethodArgumentNotValidException;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebSocketExceptionHandler {

    @MessageExceptionHandler(MethodArgumentNotValidException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public WebSocketError handleValidation(MethodArgumentNotValidException exception) {
        return new WebSocketError(
                WebSocketErrorCode.MESSAGE_INVALID_CONTENT,
                "Message must contain from 1 to 300 characters"
        );
    }

    @MessageExceptionHandler(WebsocketServiceException.class)
    @SendToUser(value = "/queue/errors", broadcast = false)
    public WebSocketError handleServiceException(
            WebsocketServiceException exception
    ) {
        return new WebSocketError(exception.getCode(), exception.getMessage());
    }


}