package messenger.backend.dtos.requests;

public record RegisterRequest(String login, String password, String username) {
}