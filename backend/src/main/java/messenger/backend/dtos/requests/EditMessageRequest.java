package messenger.backend.dtos.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record EditMessageRequest(
        @NotBlank
        @Size(max = 300)
        String content
) {
}