package messenger.backend.controllers;

import messenger.backend.generated.api.UsersApi;
import messenger.backend.generated.model.UserSearchResponse;
import messenger.backend.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UsersController implements UsersApi {

    private final UserService userService;

    public UsersController(UserService userService) {
        this.userService = userService;
    }

    @Override
    public ResponseEntity<UserSearchResponse> searchUsers(String username, Integer limit) {
        UserSearchResponse response = userService.searchUsers(username, limit);

        return ResponseEntity.ok(response);
    }
}
