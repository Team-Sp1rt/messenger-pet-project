package messenger.backend.dtos;

public record WebSocketError(
        WebSocketErrorCode code,
        String message
) {}