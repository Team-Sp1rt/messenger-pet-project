package messenger.backend.dtos;

import messenger.backend.generated.model.Message;

public record MessageChangedEvent(
        ChatEventType type,
        Message message
) {}
