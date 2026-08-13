package messenger.backend.dtos;

import java.time.LocalDate;

public record User(Long id, String username, String bio, LocalDate birthday) {
}
