package messenger.backend.dtos;

import jakarta.validation.constraints.NotBlank;

public record SendMessageRequest(
        @NotBlank
        String content

) {
}
