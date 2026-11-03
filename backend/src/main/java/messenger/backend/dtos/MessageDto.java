package messenger.backend.dtos;

import java.sql.Timestamp;

public record MessageDto(Long id, Long chatID, Long userID, String content, Timestamp createdAt) {
}
