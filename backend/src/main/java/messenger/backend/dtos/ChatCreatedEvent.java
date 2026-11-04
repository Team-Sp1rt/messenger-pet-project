package messenger.backend.dtos;

import messenger.backend.generated.model.Chat;

public record ChatCreatedEvent(
        ChatEventType type,
        Chat chat
) {}