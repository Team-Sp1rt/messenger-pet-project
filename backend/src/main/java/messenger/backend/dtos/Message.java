package messenger.backend.dtos;

import java.sql.Timestamp;

public record Message(long id, long chatID, long userID, String content, Timestamp timestamp) {
}
