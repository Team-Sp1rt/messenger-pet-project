package messenger.backend.services;

import messenger.backend.dtos.*;
import messenger.backend.exceptions.services.WebsocketServiceException;
import messenger.backend.messaging.WebSocketMessagePublisher;
import messenger.backend.repositories.MessagesRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.MessagingException;
import org.springframework.stereotype.Service;

import java.sql.SQLException;


@Service
public class WebsocketService {
    private static final Logger log = LoggerFactory.getLogger(WebsocketService.class);

    private final ChatMembershipService chatMembershipService;
    private final MessagesRepository messagesRepository;

    private final WebSocketMessagePublisher messagePublisher;

    public WebsocketService(ChatMembershipService chatMembershipService,
                            MessagesRepository mesRepository,
                            WebSocketMessagePublisher messagePublisher) {
        this.chatMembershipService = chatMembershipService;
        this.messagesRepository = mesRepository;
        this.messagePublisher = messagePublisher;
    }

    public void sendMessage(long chatId, long userId,
                            String content) throws WebsocketServiceException {

        chatMembershipService.checkUserInChat(userId, chatId);
        messenger.backend.dtos.Message message;

        try {
            message = messagesRepository.insertNewMessageReturnsMessage(new NewMessage(chatId, userId, content));

        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, "Couldn't send message due to unknown reason", e);
        }

        try {
            messagePublisher.publishCreated(message);
        } catch (MessagingException e) {
            log.error("Failed to publish MESSAGE_CREATED: chatId={}, messageId={}",
                    chatId,
                    message.id(), e);
        }

    }

    public void editMessage(long chatId, long userId,
                            long messageId,
                            String content) throws WebsocketServiceException {

        chatMembershipService.checkUserInChat(userId, chatId);
        messenger.backend.dtos.Message message;

        try {

            if (messagesRepository.getUserIDByMessageID(messageId)!=userId) {
                throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, "You can't edit this message");
            }

            message = messagesRepository.editMessageReturnsMessage(messageId, content);

        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, "Couldn't edit message due to unknown reason", e);
        }

        try {
            messagePublisher.publishUpdated(message);
        } catch (MessagingException e) {
            log.error("Failed to publish MESSAGE_CREATED: chatId={}, messageId={}",
                    chatId,
                    messageId, e);
        }

    }


    public void deleteMessage(long chatId, long userId,
                            long messageId) throws WebsocketServiceException {
        try {

            chatMembershipService.checkUserInChat(userId, chatId);

            if (messagesRepository.getUserIDByMessageID(messageId)!=userId) {
                throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_ACCESS_DENIED, "You can't delete this message");
            }

            messagesRepository.deleteMessage(messageId);

        } catch (SQLException e) {
            throw new WebsocketServiceException(WebSocketErrorCode.MESSAGE_OPERATION_FAILED, "Couldn't delete message due to unknown reason", e);
        }

        try {
            messagePublisher.publishDeleted(chatId, messageId);
        } catch (MessagingException e) {
            log.error("Failed to publish MESSAGE_CREATED: chatId={}, messageId={}",
                    chatId,
                    messageId, e);
        }

    }

}
