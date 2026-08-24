package messenger.backend.controllers;

import messenger.backend.generated.api.ChatsApi;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.ChatListResponse;
import messenger.backend.generated.model.CreateChatRequest;
import messenger.backend.generated.model.MessagePage;
import messenger.backend.services.ChatService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatsController implements ChatsApi {

    private final ChatService chatService;

    public ChatsController(ChatService chatService) {
        this.chatService = chatService;
    }

    @Override
    public ResponseEntity<Chat> createChat(CreateChatRequest createChatRequest) {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        long creatorId = Long.parseLong(authentication.getToken().getSubject());

        Chat newChat = chatService.createChat(createChatRequest, creatorId);

        return ResponseEntity.ok(newChat);
    }

    @Override
    public ResponseEntity<MessagePage> getChatMessages(Long chatId, Long beforeMessageId, Integer limit) {
        MessagePage messagePage = chatService.getChatMessages(chatId, beforeMessageId, limit);

        return ResponseEntity.ok(messagePage);
    }

    @Override
    public ResponseEntity<ChatListResponse> getChats() {
        JwtAuthenticationToken authentication = (JwtAuthenticationToken) SecurityContextHolder
                .getContext()
                .getAuthentication();

        long userId = Long.parseLong(authentication.getToken().getSubject());

        return ResponseEntity.ok(chatService.getChats(userId));
    }

}
