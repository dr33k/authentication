package com.seven.auth;

import com.seven.auth.exception.AuthorizationException;
import com.seven.auth.util.Constants;
import com.seven.auth.util.response.Response;
import com.seven.auth.account.*;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.seven.auth.util.response.Responder.created;
import static com.seven.auth.util.response.Responder.ok;

@RestController
@RequestMapping("/auth")
@SecurityRequirements
public class JwtAuthController {
    JwtService jwtService;

    public JwtAuthController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @PostMapping(value = "/register", produces = "application/json", consumes = "application/json")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Response> createResource(@Valid @RequestBody AccountDTO.Create request) throws AuthorizationException {
            AuthDTO userDTO = jwtService.provisionSuper(request);
            return created(userDTO.data, userDTO.token, "/domains" );
    }

    @PostMapping(value = "/login", produces = "application/json", consumes = "application/json")
    @Parameter(name = Constants.TENANT_ID_KEY, in = ParameterIn.HEADER, required = true)
    public ResponseEntity<Response> login(@Valid @RequestBody JwtLoginRequest request) throws AuthorizationException {
        AuthDTO userDTO = jwtService.login(request);
        return ok(userDTO.data, userDTO.token);
    }
}
