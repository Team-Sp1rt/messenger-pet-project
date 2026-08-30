package messenger.backend.services;

import messenger.backend.dtos.Message;
import messenger.backend.dtos.User;
import messenger.backend.exceptions.repostitories.NoSuchMessageException;
import messenger.backend.exceptions.repostitories.NoSuchUserException;
import messenger.backend.exceptions.services.DatabaseException;
import messenger.backend.generated.model.*;
import messenger.backend.repositories.ChatMembersRepository;
import messenger.backend.repositories.ChatsRepository;
import messenger.backend.repositories.MessagesRepository;
import messenger.backend.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.sql.SQLException;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


@Service
public class ChatService {
    private static final Logger log = LoggerFactory.getLogger(ChatService.class);

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

            // TODO: Тут тоже можно использовать новый метод,
            //  особенно когда будем добавлять групповые чаты,
            //  но уходит NoSuchUserException :/
            List<UserSummary> members = List.of(
                    userRepository.getUserSummaryByID(creatorId),
                    userRepository.getUserSummaryByID(request.getMemberId()));

            return new Chat(chatId, members);
        } catch (SQLException e) {
            throw new DatabaseException("CreateChat failed due to SQLException: " + e.getMessage(), e);
        } catch (NoSuchUserException e) {
            throw new DatabaseException(
                    "NO_SUCH_USER", HttpStatus.NOT_FOUND,
                    "CreateChat failed due to NoSuchUserException: " + e.getMessage(), e
            );
        }
    }

    public MessagePage getChatMessages(Long chatId, Long beforeMessageId, Integer limit) {
        List<messenger.backend.dtos.Message> dbMessages;
        try {
            if (beforeMessageId == null) {
                dbMessages = messagesRepository.getLastNMessagesInTheChat(limit + 1, chatId);
            } else {
                dbMessages = messagesRepository.getNMessagesInTheChatBeforeMessageWithID(limit + 1, chatId, beforeMessageId);
            }
        } catch (SQLException e) {
            throw new DatabaseException("GetChatMessages failed due to SQLException: " + e.getMessage(), e);
        } catch (NoSuchMessageException e) {
            throw new DatabaseException(
                    "NO_SUCH_MESSAGE", HttpStatus.NOT_FOUND,
                    "GetChatMessages failed due to NoSuchMessageException: " + e.getMessage(), e
            );
        }

        boolean hasMore = dbMessages.size() > limit;

        if (hasMore) {
            dbMessages = new ArrayList<>(dbMessages.subList(0, limit));
        }

        List<messenger.backend.generated.model.Message> apiMessages = dbMessages.stream().map(this::toApiMessage).toList();

        return hasMore ? new MessagePage(apiMessages, apiMessages.getLast().getId())
                : new MessagePage(apiMessages, null);
    }

    public ChatListResponse getChats(Long userId) {
        try {
            Set<Long> chatIds = chatMembersRepository.getAllChatsOfTheMember(userId);
            List<ChatSummary> chatSummariesList = new ArrayList<>();

            for (Long id : chatIds) {
                try {
                    Set<Long> memberIds = chatMembersRepository.getAllMembersOfTheChat(id);
                    List<UserSummary> userSummaryList = userRepository.getUserSummariesByIDs(memberIds);
                    List<Message> messages = messagesRepository.getLastNMessagesInTheChat(1, id);
                    chatSummariesList.add(new ChatSummary(id, userSummaryList, messages.isEmpty() ? null : toApiMessage(messages.getFirst())));
                } catch (SQLException e) {
                    log.error("Failed to get chat: chatId={}", id, e);
                }
            }
            return new ChatListResponse(chatSummariesList);
        } catch (SQLException e) {
            throw new DatabaseException("GetChatList failed due to SQLException: " + e.getMessage(), e);
        }
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

    @Deprecated
    private UserSummary toUserSummary(User user) {
        return new UserSummary(user.id(), user.username());
    }
}
