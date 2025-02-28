package com.seven.auth.security.authentication.jwt;

import com.seven.auth.response.Response;
import com.seven.auth.account.*;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.seven.auth.response.Responder.created;
import static com.seven.auth.response.Responder.ok;

@RestController
@RequestMapping("auth/jwt")
public class JwtAuthController {
    JwtService jwtService;
    AuthenticationProvider authenticationProvider;

    public JwtAuthController(JwtService jwtService, AuthenticationProvider authenticationProvider) {
        this.jwtService = jwtService;
        this.authenticationProvider = authenticationProvider;
    }

    @PostMapping(value = "/register", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> createResource(@Valid @RequestBody AccountRequest.Create request) {
            AccountDTO userDTO = jwtService.register(request);
            return created(userDTO.data, userDTO.token);
    }

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    public ResponseEntity<Response> login(@Valid @RequestBody JwtLoginRequest request){
        Account account = (Account) authenticationProvider.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())).getPrincipal();

        AccountDTO userDTO = jwtService.login(account);
        return ok(userDTO.data, userDTO.token);
    }
}
