package com.seven.auth.account;

import com.seven.auth.application.ApplicationDTO;
import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.Constants;
import com.seven.auth.util.response.Response;
import com.seven.auth.annotation.Authorize;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.headers.Header;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

import static com.seven.auth.util.response.Responder.noContent;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/accounts")
public class AccountController {
    AccountService accountService;
    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @GetMapping("{accountId}")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER)
    public ResponseEntity <Response> getResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id) throws AuthorizationException {
        AccountDTO.Record accountRecord = accountService.get(id);
        return ok(accountRecord);
    }

    @PutMapping("{accountId}")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER)
    public ResponseEntity <Response> updateResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id, @Valid @RequestBody AccountDTO.Update request) throws AuthorizationException {
        AccountDTO.Record accountRecord = accountService.update(id, request);
        return ok(accountRecord);
    }

    @PostMapping
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER)
    public ResponseEntity <Response> createResource(@Valid @RequestBody AccountDTO.Create create) throws AuthorizationException {
        AccountDTO.Record record = accountService.create(create);
        return ok(record);
    }

    @DeleteMapping("{accountId}")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER)
    public ResponseEntity <Response> deleteResource(@Valid @NotNull @PathVariable(value = "accountId") UUID id) throws AuthorizationException  {
        accountService.delete(id);
        return noContent();
    }
}