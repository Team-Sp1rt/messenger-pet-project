package messenger.backend.messaging;

import messenger.backend.dtos.Message;

public interface MessageEventPublisher {

    void publishCreated(Message message);

    void publishUpdated(Message message);

    void publishDeleted(Long chatId, Long messageId);
}