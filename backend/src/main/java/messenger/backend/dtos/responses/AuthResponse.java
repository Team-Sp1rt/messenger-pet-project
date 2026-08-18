package messenger.backend.dtos.responses;

public record AuthResponse(
        String token,
        UserResponse user
) {}