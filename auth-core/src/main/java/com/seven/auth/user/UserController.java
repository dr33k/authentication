package com.seven.auth.user;

import com.seven.auth.response.Response;
import com.seven.auth.security.authorization.Authorize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.response.Responder.noContent;
import static com.seven.auth.response.Responder.ok;

@RestController
@RequestMapping("/user")
public class UserController {
    UserService userService;
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Authorize(roles = {"ROLE_ADMIN", "ADMIN"})
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "userId") UUID id) {
        UserRecord userRecord = userService.get(id); //Signifies account owner access
        return ok(userRecord);
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "userId") UUID id, @Valid @RequestBody UserUpdateRequest request) {
        UserRecord userRecord = userService.update(id, request);
        return ok(userRecord);
    }
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "userId") UUID id) {
        userService.delete(id);
        return noContent();
    }
}