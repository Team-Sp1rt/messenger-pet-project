package messenger.backend.dtos;

import messenger.backend.dtos.responses.MessageResponse;

public record MessageChangedEvent(
        ChatEventType type,
        MessageResponse message
) {}
