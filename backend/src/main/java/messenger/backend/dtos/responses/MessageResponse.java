package messenger.backend.dtos.responses;

import java.time.Instant;

public record MessageResponse(
        long id,
        long chatId,
        long userId,
        String content,
        Instant createdAt
) {
}