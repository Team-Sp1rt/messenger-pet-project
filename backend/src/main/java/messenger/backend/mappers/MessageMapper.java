package messenger.backend.mappers;

import messenger.backend.dtos.MessageDto;
import messenger.backend.generated.model.Message;

import java.time.ZoneOffset;

public final class MessageMapper {

    private MessageMapper() {
    }

    public static Message toApi(MessageDto message) {
        return new Message(
                message.id(),
                message.chatID(),
                message.userID(),
                message.content(),
                message.createdAt()
                        .toInstant()
                        .atOffset(ZoneOffset.UTC)
        );
    }
}