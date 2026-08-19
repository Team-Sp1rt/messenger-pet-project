package messenger.backend.dtos.requests;

import java.time.LocalDate;

public record RegistrationRequest(String username, String login, String password, LocalDate birthday) {
}