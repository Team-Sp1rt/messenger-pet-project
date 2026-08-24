package messenger.backend.services;

import messenger.backend.dtos.*;
import messenger.backend.exceptions.repostitories.messages.NoSuchMessageException;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.MessagesRepository;
import messenger.backend.generated.model.Message;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.sql.SQLException;
import java.time.ZoneOffset;


@Service
public class WebsocketService {

    private final ChatMembersRepository chatMembersRepository;
    private final MessagesRepository messagesRepository;

    private final SimpMessagingTemplate simpMessagingTemplate;

    public WebsocketService(ChatMembersRepository chMemRepository,
                            MessagesRepository mesRepository,
                            SimpMessagingTemplate simpMessagingTemplate) {
        this.chatMembersRepository = chMemRepository;
        this.messagesRepository = mesRepository;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public void sendMessage(long chatId, long userId,
                            String content) throws WebsocketServiceException {

//        try {
//            if (!chatMembersRepository.getAllChatsOfTheMember(userId).contains(chatId)) {
//                throw new WebsocketServiceException(WebSocketErrorCode.CHAT_NOT_FOUND, "Chat not found");
//            }
//        } catch (SQLException e) {
//            throw new WebsocketServiceException(WebSocketErrorCode.CHAT_NOT_FOUND, e.getMessage());
//        }

        try {
            if (!chatMembersRepository.getAllMembersOfTheChat(chatId).contains(userId)) {
                throw new WebsocketServiceException(WebSocketErrorCode.CHAT_ACCESS_DENIED, "User is not a member of this chat");
            }
        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.CHAT_ACCESS_DENIED, e.getMessage());
        }

        try {
            messenger.backend.dtos.Message message = messagesRepository.insertNewMessageReturnsMessage(new NewMessage(chatId, userId, content));
            simpMessagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                    new MessageChangedEvent(ChatEventType.MESSAGE_CREATED,
                            convertToMessage(message)));
        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, e.getMessage());
        }

    }

    public void editMessage(long chatId, long userId,
                            long messageId,
                            String content) throws WebsocketServiceException {
        try {
            if (messagesRepository.getUserIDByMessageID(messageId)!=userId) {
                throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, "You can't edit this message");
            }

            messenger.backend.dtos.Message message = messagesRepository.editMessageReturnsMessage(messageId, content);
            simpMessagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                    new MessageChangedEvent(ChatEventType.MESSAGE_UPDATED,
                            convertToMessage(message)));

        } catch (SQLException | NoSuchMessageException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_NOT_FOUND, "We can't found your message for edit");
        }

    }


    public void deleteMessage(long chatId, long userId,
                            long messageId) throws WebsocketServiceException {
        try {
            if (messagesRepository.getUserIDByMessageID(messageId)!=userId) {
                throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, "You can't delete this message");
            }

            messagesRepository.deleteMessage(messageId);
            simpMessagingTemplate.convertAndSend("/topic/chats/" + chatId + "/events",
                    new MessageDeletedEvent(ChatEventType.MESSAGE_DELETED,
                                    chatId,
                                    messageId));

        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, "Couldn't delete message due to unknown reason");
        } catch (NoSuchMessageException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_NOT_FOUND, e.getMessage());
        }
    }

    private Message convertToMessage(messenger.backend.dtos.Message message) {
        return new Message(
                message.id(),
                message.chatID(),
                message.userID(),
                message.content(),
                message.createdAt().
                        toInstant().
                        atOffset(ZoneOffset.UTC));
    }
}
