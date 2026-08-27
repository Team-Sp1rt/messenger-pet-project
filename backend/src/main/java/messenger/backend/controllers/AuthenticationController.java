package messenger.backend.controllers;

import messenger.backend.dtos.requests.AuthorisationRequest;
import messenger.backend.dtos.requests.RegistrationRequest;
import messenger.backend.dtos.responses.AuthResponse;
import messenger.backend.dtos.responses.RefreshResponse;
import messenger.backend.services.AuthorisationService;
import messenger.backend.services.JwtService;
import messenger.backend.services.RegistrationService;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private static final String BEARER_PREFIX = "Bearer ";

    private final RegistrationService registrationService;
    private final AuthorisationService authorisationService;

    private final JwtDecoder jwtDecoder;
    private final JwtService jwtService;

    public AuthenticationController(
            RegistrationService registrationService,
            AuthorisationService authorisationService,
            JwtService jwtService,
            JwtDecoder jwtDecoder) {
        this.registrationService = registrationService;
        this.authorisationService = authorisationService;
        this.jwtService = jwtService;
        this.jwtDecoder = jwtDecoder;
    }

    @PostMapping("/register")
    public AuthResponse postRegisterUser(@RequestBody RegistrationRequest registerRequest) {
        return registrationService.registerUser(registerRequest);
    }

    @PostMapping("/login")
    public AuthResponse postLoginUser(@RequestBody AuthorisationRequest authorisationRequest) {
        return authorisationService.getUserByLoginAndPassword(authorisationRequest);
    }

    @PostMapping("/refresh")
    public RefreshResponse postRefreshToken(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(BEARER_PREFIX.length()).trim();

        Jwt jwt = jwtDecoder.decode(token);

        String userId = jwt.getSubject();
        String username = jwt.getClaimAsString("username");

        String newToken = jwtService.generateAccessToken(Long.parseLong(userId), username);

        return new RefreshResponse(newToken);
    }
}