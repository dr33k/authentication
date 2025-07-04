package com.seven.auth.account;

import com.seven.auth.util.response.Response;
import com.seven.auth.util.annotation.Authorize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.util.response.Responder.noContent;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/account")
public class AccountController {
    AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    @Authorize(roles = {"ROLE_ADMIN", "ADMIN"})
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id) {
        AccountRecord accountRecord = accountService.get(id); //Signifies account owner access
        return ok(accountRecord);
    }

    @PutMapping("{accountId}")
    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id, @Valid @RequestBody AccountRequest.Update request) {
        AccountRecord accountRecord = accountService.update(id, request);
        return ok(accountRecord);
    }
    @DeleteMapping("{accountId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id) {
        accountService.delete(id);
        return noContent();
    }
}