package messenger.backend.dtos.responses;

public record ErrorResponse(
        String error,
        String message,
        int status
) {}