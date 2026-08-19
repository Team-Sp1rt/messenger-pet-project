package messenger.backend.dtos.responses;

import messenger.backend.dtos.User;

public record AuthResponse(
        String token,
        User user
) {}