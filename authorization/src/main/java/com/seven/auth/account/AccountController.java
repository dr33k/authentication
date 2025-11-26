package com.seven.auth.account;

import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.response.Response;
import com.seven.auth.annotation.Authorize;
import io.swagger.v3.oas.annotations.headers.Header;
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

    @GetMapping("{accountId}")
    public ResponseEntity <Response> getResource(@Valid @NotNull @RequestHeader UUID tenantId, @Valid @NotNull @PathVariable(value = "accountId") UUID id) {
        AccountDTO.Record accountRecord = accountService.get(id);
        return ok(accountRecord);
    }

    @PutMapping("{accountId}")
    public ResponseEntity <Response> updateResource(@Valid @NotNull @RequestHeader UUID tenantId, @Valid @NotNull @PathVariable(value = "accountId") UUID id, @Valid @RequestBody AccountDTO.Update request) {
        AccountDTO.Record accountRecord = accountService.update(id, request);
        return ok(accountRecord);
    }

    @PostMapping
    public ResponseEntity <Response> createResource(@Valid @NotNull @RequestHeader UUID tenantId, @Valid @RequestBody AccountDTO.Create create){
        AccountDTO.Record record = accountService.create(create);
        return ok(record);
    }

    @DeleteMapping("{accountId}")
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @RequestHeader UUID tenantId, @Valid @NotNull @PathVariable(value = "accountId") UUID id) {
        accountService.delete(id);
        return noContent();
    }
}