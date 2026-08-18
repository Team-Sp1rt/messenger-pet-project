package messenger.backend.controllers;

import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.services.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final RegistrationService registrationService;

    public AuthenticationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public AuthResponse sayHello(@RequestBody RegistrationRequest registerRequest) {
        return registrationService.registerUser(
                registerRequest.username(),
                registerRequest.login(),
                registerRequest.password(),
                LocalDate.of(2000, 1, 1)
        );
    }

}