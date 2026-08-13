package messenger.backend.dtos;

import java.sql.Timestamp;

public record Message(Long id, Long chatID, Long userID, String content, Timestamp createdAt) {
}
