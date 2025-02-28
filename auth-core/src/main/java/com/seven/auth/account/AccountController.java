package com.seven.auth.account;

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
public class AccountController {
    AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @Authorize(roles = {"ROLE_ADMIN", "ADMIN"})
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "userId") UUID id) {
        UserRecord userRecord = accountService.get(id); //Signifies account owner access
        return ok(userRecord);
    }

    @PutMapping("/update/{userId}")
    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "userId") UUID id, @Valid @RequestBody AccountUpdateRequest request) {
        UserRecord userRecord = accountService.update(id, request);
        return ok(userRecord);
    }
    @DeleteMapping("/delete/{userId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "userId") UUID id) {
        accountService.delete(id);
        return noContent();
    }
}