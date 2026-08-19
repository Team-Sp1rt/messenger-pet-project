package messenger.backend.controllers;

import messenger.backend.dtos.requests.AuthorisationRequest;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.services.AuthorisationService;
import messenger.backend.services.RegistrationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private final RegistrationService registrationService;
    private final AuthorisationService authorisationService;

    public AuthenticationController(
            RegistrationService registrationService,
            AuthorisationService authorisationService) {
        this.registrationService = registrationService;
        this.authorisationService = authorisationService;
    }

    @PostMapping("/register")
    public AuthResponse postRegisterUser(@RequestBody RegistrationRequest registerRequest) {
        return registrationService.registerUser(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse postLoginUser(@RequestBody AuthorisationRequest authorisationRequest) {
        return authorisationService.getUserByLoginAndPassword(authorisationRequest);
    }
}