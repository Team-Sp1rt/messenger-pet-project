package messenger.backend.dtos;

import java.time.LocalDate;

public record User(long id, String username, String bio, LocalDate birthday) {
}
