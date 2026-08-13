package messenger.backend.dtos;

public record NewMessage(Long chatID, Long userID, String content) {
}
