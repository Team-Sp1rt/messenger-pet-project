package messenger.backend.controllers;

import jakarta.validation.Valid;
import messenger.backend.dtos.requests.EditMessageRequest;
import messenger.backend.dtos.requests.SendMessageRequest;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.services.WebsocketService;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class WebSocketController {

    private final WebsocketService websocketService;

    public WebSocketController(WebsocketService websocketService) {
        this.websocketService = websocketService;
    }

    @MessageMapping("/chats/{chatId}/messages")
    public void sendNewMessage(@DestinationVariable long chatId,
            @Valid SendMessageRequest request,
            Principal principal) throws WebsocketServiceException {
        long userId = Long.parseLong(principal.getName());

        websocketService.sendMessage(chatId, userId, request.content());
    }

    @MessageMapping("/chats/{chatId}/messages/{messageId}/edit")
    public void editeMessage(@DestinationVariable long chatId,
                             @DestinationVariable long messageId,
                             @Valid EditMessageRequest request,
                             Principal principal) throws WebsocketServiceException {
        long userId = Long.parseLong(principal.getName());

        websocketService.editMessage(chatId, userId, messageId, request.content());
    }

    @MessageMapping("/chats/{chatId}/messages/{messageId}/delete")
    public void deleteMessage(@DestinationVariable long chatId,
                              @DestinationVariable long messageId,
                              Principal principal) throws WebsocketServiceException {

        long userId = Long.parseLong(principal.getName());

        websocketService.deleteMessage(chatId, userId, messageId);
    }
}
