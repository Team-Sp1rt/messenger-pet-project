package messenger.backend.dtos;

import java.time.Instant;

// потому что на фронт нужен id строкой
public record MessageResponse(
        String id,
        String chatId,
        String userId,
        String content,
        Instant createdAt
) {
}