package messenger.backend.controllers;

import messenger.backend.dtos.requests.RegisterRequest;
import messenger.backend.services.RegistrationService;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@CrossOrigin(origins = "*")
@RequestMapping("/api/auth")
public class AuthController {
    private final RegistrationService registrationService;

    public AuthController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/register")
    public String sayHello(@RequestBody RegisterRequest registerRequest) {
        registrationService.registerUser(
                registerRequest.username(),
                registerRequest.login(),
                registerRequest.password(),
                LocalDate.of(2000, 1, 1)
        );

        return "Welcome to Messenger";
    }

}