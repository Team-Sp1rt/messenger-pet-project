package messenger.backend.services;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.messages.NoSuchMessageException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.generated.model.Chat;
import messenger.backend.generated.model.CreateChatRequest;
import messenger.backend.generated.model.MessagePage;
import messenger.backend.generated.model.UserSummary;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.ChatsRepository;
import messenger.backend.repositories.MessagesRepository;
import messenger.backend.repositories.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;


@Service
public class ChatService {
    private final ChatsRepository chatsRepository;
    private final ChatMembersRepository chatMembersRepository;
    private final UserRepository userRepository;
    private final MessagesRepository messagesRepository;

    public ChatService(ChatsRepository chatsRepository, ChatMembersRepository chatMembersRepository,
                       UserRepository userRepository, MessagesRepository messagesRepository) {
        this.chatsRepository = chatsRepository;
        this.chatMembersRepository = chatMembersRepository;
        this.userRepository = userRepository;
        this.messagesRepository = messagesRepository;
    }

    @Transactional
    public Chat createChat(CreateChatRequest request, long creatorId) {
        try {
            long chatId = chatsRepository.insertNewChatReturnsChatID();
            chatMembersRepository.insertNewChatMember(chatId, request.getMemberId());
            chatMembersRepository.insertNewChatMember(chatId, creatorId);

            List<UserSummary> members = List.of(
                    toUserSummary(userRepository.getUserByID(creatorId)),
                    toUserSummary(userRepository.getUserByID(request.getMemberId())));

            return new Chat(chatId, members);
        } catch (SQLException e) {
            throw new DatabaseException("CreateChat failed due to SQLException: ", e);
        }
    }

    public MessagePage getChatMessages(Long chatId, Long beforeMessageId, Integer limit, long userId) {
        List<messenger.backend.dtos.Message> dbMessages;
        try {
            if (beforeMessageId == null) {
                dbMessages = messagesRepository.getLastNMessagesInTheChat(limit + 1, chatId);
            } else {
                // TODO: использовать метод, который напише ваня
                //   dbMessages = messagesRepository.getNMessagesInTheChatBeforeMessage(limit + 1, chatId, beforeMessageId);
                Message beforeMessage = messagesRepository.getMessageById(beforeMessageId);
                dbMessages = messagesRepository.getNMessagesInTheChatBeforeMessage(limit + 1, chatId, beforeMessage);
            }
        } catch (NoSuchMessageException e) {
            throw new DatabaseException("beforeMessageId is invalid", e);
        } catch (SQLException e) {
            throw new DatabaseException("GetChatMessages failed due to SQLException: ", e);
        }

        boolean hasMore = dbMessages.size() > limit;

        if (hasMore) {
            dbMessages = new ArrayList<>(dbMessages.subList(0, limit));
        }

        List<messenger.backend.generated.model.Message> apiMessages = dbMessages.stream().map(this::toApiMessage).toList();

        return hasMore ? new MessagePage(apiMessages, apiMessages.getLast().getId())
                : new MessagePage(apiMessages, null);
    }

    private messenger.backend.generated.model.Message toApiMessage(
            messenger.backend.dtos.Message message
    ) {
        return new messenger.backend.generated.model.Message(
                message.id(),
                message.chatID(),
                message.userID(),
                message.content(),
                message.createdAt()
                        .toInstant()
                        .atOffset(ZoneOffset.UTC)
        );
    }

    private UserSummary toUserSummary(User user) {
        return new UserSummary(user.id(), user.username());
    }
}
