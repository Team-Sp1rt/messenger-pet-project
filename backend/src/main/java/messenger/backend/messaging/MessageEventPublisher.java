package messenger.backend.messaging;

import messenger.backend.dtos.MessageDto;

public interface MessageEventPublisher {

    void publishCreated(MessageDto message);

    void publishUpdated(MessageDto message);

    void publishDeleted(Long chatId, Long messageId);
}