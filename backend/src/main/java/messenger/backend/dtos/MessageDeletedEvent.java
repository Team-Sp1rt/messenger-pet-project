package messenger.backend.dtos;

public record MessageDeletedEvent(
        ChatEventType type,
        long chatId,
        long messageId
) {}