package messenger.backend.services;

import messenger.backend.dtos.WebSocketErrorCode;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.repositories.ChatMembersRepository;
import org.springframework.stereotype.Service;

import java.sql.SQLException;

@Service
public class ChatMembershipService {

    private final ChatMembersRepository chatMembersRepository;

    public ChatMembershipService(ChatMembersRepository chatMembersRepository) {
        this.chatMembersRepository = chatMembersRepository;
    }

    public void checkUserInChat(long userId, long chatId) {
        try {
            if (!chatMembersRepository.getAllMembersOfTheChat(chatId).contains(userId)) {
                throw new WebsocketServiceException(
                        WebSocketErrorCode.CHAT_ACCESS_DENIED,
                        "User is not a member of this chat"
                );
            }
        } catch (SQLException exception) {
            throw new WebsocketServiceException(
                    WebSocketErrorCode.CHAT_ACCESS_DENIED,
                    exception.getMessage()
            );
        }
    }
}